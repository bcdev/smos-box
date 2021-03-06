package org.esa.beam.smos.ee2netcdf;

import com.vividsolutions.jts.geom.Geometry;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProducts;
import org.esa.beam.util.converters.JtsGeometryConverter;

import java.io.File;

abstract class AbstractNetCDFExporterOp extends Operator {

    @SourceProducts(type = ExportParameter.PRODUCT_TYPE_REGEX,
            description = "The source products to be converted. If not given, the parameter 'sourceProductPaths' must be provided.")
    protected Product[] sourceProducts;

    @Parameter(description = "Comma-separated list of file paths specifying the source products.\n" +
            "Each path may contain the wildcards '**' (matches recursively any directory),\n" +
            "'*' (matches any character sequence in path names) and\n" +
            "'?' (matches any single character).")
    protected String[] sourceProductPaths;

    @Parameter(description = "The target directory for the converted data. If not existing, directory will be created.",
            defaultValue = ".",
            notEmpty = true,
            notNull = true)
    protected File targetDirectory;

    @Parameter(defaultValue = "false",
            description = "Set true to overwrite already existing target files.")
    protected boolean overwriteTarget;

    @Parameter(description = "Target geographical region as a geometry in well-known text format (WKT). The output product will be tailored according to the region.",
            converter = JtsGeometryConverter.class)
    protected Geometry region;

    protected void setDummyTargetProduct() {
        final Product product = new Product("dummy", "dummy", 2, 2);
        product.addBand("dummy", ProductData.TYPE_INT8);
        setTargetProduct(product);
    }
}
