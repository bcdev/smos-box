package org.esa.beam.smos.ee2netcdf;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.esa.beam.framework.gpf.annotations.Parameter;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.logging.Level;

/**
 * @author Ralf Quast
 */
public class GPToNetCDFExporterTool {

    private static final Level[] LOG_LEVELS = new Level[]{
            Level.ALL,
            Level.INFO,
            Level.CONFIG,
            Level.WARNING,
            Level.SEVERE,
            Level.OFF
    };

    private static final String LOG_LEVEL_DESCRIPTION = "Set the logging level to <level> where <level> must be in "
                                                        + Arrays.toString(LOG_LEVELS)
                                                        + ". The default logging level is '"
                                                        + Level.INFO
                                                        + "'.";
    private static final String LOG_LEVEL_LONGOPT = "log-level";

    private Options options;
    private CommandLine commandLine;
    private boolean produceErrorMessages;

    public static void main(String[] args) {
        new GPToNetCDFExporterTool().run(args);
    }

    private void run(String[] arguments) {
        options = createOptions();
        try {
            run0(arguments);
        } catch (Throwable e) {
            printHelp();
            exit(e, 1);
        }
    }

    private void exit(Throwable e, int exitCode) {
        // TODO ...
        System.exit(exitCode);
    }

    private void run0(String[] arguments) throws ParseException {
        commandLine = parseCommandLine(arguments);

        if (commandLine.getArgs().length == 0 || commandLine.getOptions().length == 0) {
            printHelp();
            return;
        }
        if (commandLine.hasOption("help")) {
            printHelp();
            return;
        }
        if (commandLine.hasOption("version")) {
            printVersion();
            return;
        }

        Level logLevel = Level.INFO;
        if (commandLine.hasOption(LOG_LEVEL_LONGOPT)) {
            logLevel = Level.parse(commandLine.getOptionValue(LOG_LEVEL_LONGOPT));
        }
        initLogger(logLevel);

        //GPT.run("");
    }

    private void initLogger(Level logLevel) {
        // TODO
    }

    private void printVersion() {
        // TODO
    }

    private void printHelp() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setWidth(80);
        helpFormatter.printHelp(getSyntax(),
                                getHeader(),
                                options,
                                getFooter(), false);
    }

    private String getFooter() {
        return "Footer";
    }

    private String getHeader() {
        return "Header";
    }

    private String getSyntax() {
        return "Syntax";
    }

    private CommandLine parseCommandLine(String[] arguments) throws ParseException {
        CommandLineParser parser = new GnuParser();
        return parser.parse(options, arguments);
    }

    private Options createOptions() {
        final Options options = new Options();

        final Field[] fields = ExportParameter.class.getDeclaredFields();
        for (final Field field : fields) {
            final Parameter parameter = field.getAnnotation(Parameter.class);
            if (parameter != null) {
                final String longOpt = parameter.label().toLowerCase().replace(" ", "-");
                OptionBuilder.withLongOpt(longOpt);

                final Class<?> type = field.getType();
                OptionBuilder.withType(type);

                final String argName = type.getSimpleName().toLowerCase();
                if (type.isAssignableFrom(boolean.class)) {
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
                final String[] valueSet = parameter.valueSet();
                if (valueSet.length != 0) {
                    descriptionBuilder
                            .append("The argument <")
                            .append(argName)
                            .append("> must be in ")
                            .append(Arrays.toString(valueSet).replace("[", "{").replace("]", "}"))
                            .append(".");
                } else {
                    final String interval = parameter.interval();
                    if (!interval.isEmpty()) {
                        descriptionBuilder
                                .append("The argument <")
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
                OptionBuilder.withDescription(descriptionBuilder.toString());
                final Option option = OptionBuilder.create();

                final boolean required = parameter.notNull() || parameter.notEmpty();
                option.setRequired(required);

                options.addOption(option);
            }
        }

        options.addOption("h", "help", false, "Display help information.");
        options.addOption("v", "version", false, "Display version information.");
        options.addOption("e", "errors", false, "Produce execution error messages.");
        OptionBuilder.withLongOpt(LOG_LEVEL_LONGOPT);
        OptionBuilder.hasArg(true);
        OptionBuilder.withType(Level.class);
        final String argName = Level.class.getSimpleName().toLowerCase();
        OptionBuilder.withArgName(argName);
        OptionBuilder.withDescription(LOG_LEVEL_DESCRIPTION);
        options.addOption(OptionBuilder.create("l"));

        return options;
    }

}
