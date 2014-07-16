package org.esa.beam.smos.ee2netcdf;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.PropertyDescriptor;
import com.bc.ceres.binding.ValidationException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.ParameterDescriptorFactory;
import org.esa.beam.smos.gui.BindingConstants;

import java.io.File;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Command line tool for converting SMOS products from Earth Explorer Format to netCDF.
 *
 * @author Ralf Quast
 */
public class GPToNetCDFExporterTool {

    private static final String TOOL_NAME = "ee2netcdf";
    private static final String TOOL_VERSION = "1.0";

    private static final int ERROR = 1;
    private static final int USAGE_ERROR = 2;
    private static final int EXECUTION_ERROR = 3;

    private static final String[] PARAMETER_NAMES = new String[]{
            BindingConstants.CONTACT,
            BindingConstants.INSTITUTION,
            BindingConstants.OVERWRITE_TARGET,
            BindingConstants.REGION,
            BindingConstants.SOURCE_DIRECTORY,
            BindingConstants.TARGET_DIRECTORY,
            BindingConstants.VARIABLES,
    };

    private static final Level[] LOG_LEVELS = new Level[]{
            Level.ALL,
            Level.INFO,
            Level.CONFIG,
            Level.WARNING,
            Level.SEVERE,
            Level.OFF
    };

    private static final String LOG_LEVEL_OPTION_NAME = "log-level";
    private static final String ERROR_OPTION_NAME = "error";
    private static final String LOG_LEVEL_DESCRIPTION = "Set the logging level to <level> where <level> must be in "
                                                        + Arrays.toString(LOG_LEVELS).replace("[", "{").replaceAll("]",
                                                                                                                   "}")
                                                        + ". The default logging level is '"
                                                        + Level.INFO.toString()
                                                        + "'.";

    private final Options options = new Options();

    private Logger logger;
    private Level logLevel = Level.INFO;
    private boolean produceErrorMessages;

    public static void main(String[] args) {
        new GPToNetCDFExporterTool().run(args);
    }

    public GPToNetCDFExporterTool() {
        defineOptions();
    }

    private void run(String[] arguments) {
        try {
            execute(arguments);
        } catch (ToolException e) {
            exit(e, e.getExitCode());
        } catch (Throwable e) {
            exit(e, ERROR);
        }
    }

    private void execute(String[] arguments) throws ToolException {
        final CommandLine commandLine;
        try {
            commandLine = parseCommandLine(arguments);

            if (commandLine.hasOption("help")) {
                printHelp();
                return;
            }
            if (commandLine.hasOption("version")) {
                printVersion();
                return;
            }
            if (commandLine.getArgs().length == 0) {
                printHelp();
                return;
            }
            if (commandLine.hasOption(ERROR_OPTION_NAME)) {
                produceErrorMessages = true;
            }
            if (commandLine.hasOption(LOG_LEVEL_OPTION_NAME)) {
                final String optionValue = commandLine.getOptionValue(LOG_LEVEL_OPTION_NAME);
                logLevel = Level.parse(optionValue);
            }
        } catch (ParseException | IllegalArgumentException e) {
            throw new ToolException(e, USAGE_ERROR);
        } finally {
            configureLogger();
        }

        final ExportParameter exportParameter = new ExportParameter();
        final PropertyContainer container = PropertyContainer.createObjectBacked(exportParameter,
                                                                                 new ParameterDescriptorFactory());
        container.setDefaultValues();
        container.setValue(BindingConstants.ROI_TYPE, BindingConstants.ROI_TYPE_GEOMETRY);

        for (final String parameterName : PARAMETER_NAMES) {
            final String optionName = getOptionName(parameterName);
            if (commandLine.hasOption(optionName)) {
                final String optionValue = commandLine.getOptionValue(optionName);
                final Property parameter = container.getProperty(parameterName);
                if (optionValue == null) {
                    if (parameter.getType().isAssignableFrom(boolean.class)) {
                        container.setValue(parameterName, true);
                        continue;
                    }
                }
                try {
                    parameter.setValueFromText(optionValue);
                } catch (ValidationException e) {
                    throw new ToolException(
                            MessageFormat.format("Missing or invalid value for option ''{0}''.", optionName), e,
                            USAGE_ERROR);
                }
            }
        }

        final GPToNetCDFExporter exporter = new GPToNetCDFExporter(exportParameter);
        try {
            exporter.initialize();
        } catch (Exception e) {
            final File targetDirectory = exportParameter.getTargetDirectory();
            throw new ToolException(
                    MessageFormat.format("The target directory ''{0}'' could not be created.", targetDirectory), e,
                    EXECUTION_ERROR);
        }
        for (final String path : commandLine.getArgs()) {
            final File file = new File(path);
            try {
                exporter.exportFile(file, getLogger());
            } catch (Exception e) {
                throw new ToolException(
                        MessageFormat.format("An error has occurred while trying to convert file ''{0}''.", path), e,
                        EXECUTION_ERROR);
            }
        }
    }

    private void exit(Throwable t, int exitCode) {
        if (produceErrorMessages) {
            System.err.println(t.getMessage());
            t.printStackTrace(System.err);
        }
        if (getLogger().isLoggable(Level.SEVERE)) {
            getLogger().log(Level.SEVERE, t.getMessage());
            if (getLogger().isLoggable(Level.FINE)) {
                for (StackTraceElement e : t.getStackTrace()) {
                    getLogger().log(Level.FINE, e.toString());
                }
            }
        }
        System.exit(exitCode);
    }

    private Logger getLogger() {
        if (logger == null) {
            logger = Logger.getAnonymousLogger(); // TODO - which logger?
        }
        return logger;
    }

    private void configureLogger() {
        getLogger().setLevel(logLevel);
    }

    private void printVersion() {
        System.out.println(TOOL_NAME + " version " + TOOL_VERSION);
    }

    private void printHelp() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setNewLine("\n");
        helpFormatter.setWidth(80);
        helpFormatter.printHelp(getSyntax(), "\nOptions:", options, "", false);
    }

    private String getSyntax() {
        return TOOL_NAME + " [options] file ...";
    }

    private CommandLine parseCommandLine(String[] arguments) throws ParseException {
        return new PosixParser().parse(options, arguments);
    }

    private void defineOptions() {
        options.addOption("e", "errors", false, "Produce execution error messages.");
        options.addOption("h", "help", false, "Display help information.");
        options.addOption("v", "version", false, "Display version information.");
        options.addOption(createOption("l", LOG_LEVEL_OPTION_NAME, Level.class, LOG_LEVEL_DESCRIPTION));

        final List<String> parameterNames = Arrays.asList(PARAMETER_NAMES);
        final Field[] fields = ExportParameter.class.getDeclaredFields();

        for (final Field field : fields) {
            final Parameter parameter = field.getAnnotation(Parameter.class);
            if (parameter != null && parameterNames.contains(parameter.alias())) {
                final String optionName = getOptionName(parameter);
                OptionBuilder.withLongOpt(optionName);

                final Class<?> type = getType(field);
                OptionBuilder.withType(type);

                final String argName = type.getSimpleName().toLowerCase();
                final boolean noArg = type.isAssignableFrom(boolean.class);
                if (noArg) {
                    OptionBuilder.hasArg(false);
                } else {
                    OptionBuilder.hasArg(true);
                    OptionBuilder.withArgName(argName);
                }

                final String description = parameter.description();
                final StringBuilder descriptionBuilder = new StringBuilder(description);
                if (!description.isEmpty() && !description.endsWith(".")) {
                    descriptionBuilder.append(".");
                }
                if (!noArg) {
                    final String[] valueSet = parameter.valueSet();
                    if (valueSet.length != 0) {
                        descriptionBuilder
                                .append(" The argument <")
                                .append(argName)
                                .append("> must be in ")
                                .append(Arrays.toString(valueSet).replace("[", "{").replace("]", "}"))
                                .append(".");
                    } else {
                        final String interval = parameter.interval();
                        if (!interval.isEmpty()) {
                            descriptionBuilder
                                    .append(" The argument <")
                                    .append(argName)
                                    .append("> must be in the interval ")
                                    .append(interval)
                                    .append(".");
                        }
                    }
                    final String defaultValue = parameter.defaultValue();
                    if (!defaultValue.isEmpty()) {
                        descriptionBuilder
                                .append(" The default value is '")
                                .append(defaultValue)
                                .append("'.");
                    }
                }
                OptionBuilder.withDescription(descriptionBuilder.toString());
                final Option option = OptionBuilder.create();

                final boolean required = parameter.notNull() || parameter.notEmpty();
                option.setRequired(required);

                options.addOption(option);
            }
        }
    }

    private static Option createOption(String opt, String optionName, Class<?> argType, String description) {
        final Option option = new Option(opt, optionName, true, description);
        option.setType(argType);
        option.setArgName(argType.getSimpleName().toLowerCase());
        return option;
    }

    private static String getOptionName(Parameter parameter) {
        return getOptionName(parameter.alias());
    }

    private static String getOptionName(String alias) {
        return PropertyDescriptor.createDisplayName(alias).replace(" ", "-").toLowerCase();
    }

    private static Class<?> getType(Field field) {
        final Class<?> fieldType = field.getType();
        if (fieldType.isPrimitive() || fieldType.equals(File.class)) {
            return fieldType;
        }
        return String.class;
    }

    private static final class ToolException extends Exception {

        private final int exitCode;

        private ToolException(Throwable cause, int exitCode) {
            super(cause);
            this.exitCode = exitCode;
        }

        private ToolException(String message, Throwable cause, int exitCode) {
            super(message, cause);
            this.exitCode = exitCode;
        }

        public int getExitCode() {
            return exitCode;
        }
    }

}
