package org.esa.beam.dataio.smos;

class ResourcePathBuilder {

    String buildPath(String identifier, String root, String appendix) {
        final String fc = identifier.substring(12, 16);
        final String sd = identifier.substring(16, 22);

        final StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(root).append("/").append(fc).append("/").append(sd).append("/").append(identifier);
        pathBuilder.append(appendix);

        return pathBuilder.toString();
    }
}
