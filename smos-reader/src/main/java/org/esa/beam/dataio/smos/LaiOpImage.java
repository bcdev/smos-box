package org.esa.beam.dataio.smos;

import com.bc.ceres.glevel.MultiLevelModel;
import com.bc.ceres.jai.NoDataRaster;
import org.esa.beam.framework.datamodel.RasterDataNode;
import org.esa.beam.jai.ImageManager;
import org.esa.beam.jai.ResolutionLevel;
import org.esa.beam.jai.SingleBandedOpImage;

import javax.media.jai.PixelAccessor;
import javax.media.jai.PlanarImage;
import javax.media.jai.UnpackedImageData;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Arrays;

class LaiOpImage extends SingleBandedOpImage {

    private final LaiFile.LaiValueProvider valueProvider;
    private final MultiLevelModel model;
    private final double noDataValue;
    private final AffineTransform imageToModelTransform;

    private volatile Area area;
    private volatile NoDataRaster noDataRaster;

    LaiOpImage(LaiFile.LaiValueProvider valueProvider, RasterDataNode rasterDataNode, MultiLevelModel model, ResolutionLevel level) {
        super(ImageManager.getDataBufferType(rasterDataNode.getDataType()),
              rasterDataNode.getSceneRasterWidth(),
              rasterDataNode.getSceneRasterHeight(),
              rasterDataNode.getProduct().getPreferredTileSize(),
              null, // no configuration
              level);

        this.valueProvider = valueProvider;
        this.model = model;
        this.noDataValue = rasterDataNode.getNoDataValue();
        this.imageToModelTransform = model.getImageToModelTransform(getLevel());
    }

    private Area getArea() {
        if (area == null) {
            synchronized (this) {
                if (area == null) {
                    final Area modelArea = valueProvider.getArea();
                    area = modelArea.createTransformedArea(model.getModelToImageTransform(getLevel()));
                }
            }
        }
        return area;
    }

    @Override
    public Raster computeTile(int tileX, int tileY) {
        if (getArea().intersects(getTileRect(tileX, tileY))) {
            return super.computeTile(tileX, tileY);
        }

        if (noDataRaster == null) {
            synchronized (this) {
                if (noDataRaster == null) {
                    noDataRaster = createNoDataRaster(noDataValue);
                }
            }
        }

        return noDataRaster.createTranslatedChild(tileXToX(tileX), tileYToY(tileY));
    }

    @Override
    protected final void computeRect(PlanarImage[] planarImages, WritableRaster targetRaster, Rectangle rectangle) {
        final PixelAccessor targetAccessor = new PixelAccessor(targetRaster.getSampleModel(), null);
        final UnpackedImageData targetData = targetAccessor.getPixels(
                targetRaster, rectangle, targetRaster.getSampleModel().getTransferType(), true);
        final PixelCounter pixelCounter = new PixelCounter(rectangle, getArea());

        switch (targetData.type) {
        case DataBuffer.TYPE_BYTE:
            byteLoop(targetData, pixelCounter, (byte) noDataValue);
            break;
        case DataBuffer.TYPE_SHORT:
        case DataBuffer.TYPE_USHORT:
            shortLoop(targetData, pixelCounter, (short) noDataValue);
            break;
        case DataBuffer.TYPE_INT:
            intLoop(targetData, pixelCounter, (int) noDataValue);
            break;
        case DataBuffer.TYPE_FLOAT:
            floatLoop(targetData, pixelCounter, (float) noDataValue);
            break;
        default:
            // do nothing
            break;
        }

        targetAccessor.setPixels(targetData);
    }

    private void byteLoop(UnpackedImageData targetData, PixelCounter pixelCounter, byte noDataValue) {
        final int w = targetData.rect.width;
        final int h = targetData.rect.height;

        final int targetPixelStride = targetData.pixelStride;
        final int targetLineStride = targetData.lineStride;
        final byte[] targetDataArray = targetData.getByteData(0);

        final long[] indexCache = new long[w];
        final byte[] valueCache = new byte[w];

        int targetLineOffset = targetData.getOffset(0);
        final Point2D point = new Point2D.Double();

        for (int y = 0; y < h; ++y) {
            int targetPixelOffset = targetLineOffset;

            pixelCounter.countPixels(targetData.rect.y + y);
            if (pixelCounter.leading > 0) {
                Arrays.fill(targetDataArray, targetPixelOffset, targetPixelOffset + pixelCounter.leading, noDataValue);
                targetPixelOffset += pixelCounter.leading * targetPixelStride;
            }
            if (pixelCounter.valid > 0) {
                for (int x = pixelCounter.leading; x < pixelCounter.leading + pixelCounter.valid; ++x) {
                    byte value;
                    final long cellIndex = computeCellIndex(targetData.rect.x + x, targetData.rect.y + y, point);

                    if (x > 0 && indexCache[x - 1] == cellIndex) {
                        // pixel to the west
                        value = valueCache[x - 1];
                    } else if (y > 0 && indexCache[x] == cellIndex) {
                        // pixel to the north
                        value = valueCache[x];
                    } else if (x + 1 < w && y > 0 && indexCache[x + 1] == cellIndex) {
                        // pixel to the north-east
                        value = valueCache[x + 1];
                    } else {
                        if (cellIndex != -1) {
                            value = valueProvider.getLaiValue(cellIndex, noDataValue);
                        } else {
                            value = noDataValue;
                        }
                    }
                    indexCache[x] = cellIndex;
                    valueCache[x] = value;

                    targetDataArray[targetPixelOffset] = value;
                    targetPixelOffset += targetPixelStride;
                }
            }
            if (pixelCounter.trailing > 0) {
                Arrays.fill(targetDataArray, targetPixelOffset, targetPixelOffset + pixelCounter.trailing, noDataValue);
            }
            targetLineOffset += targetLineStride;
        }
    }

    private void shortLoop(UnpackedImageData targetData, PixelCounter pixelCounter, short noDataValue) {
        final int w = targetData.rect.width;
        final int h = targetData.rect.height;

        final int targetPixelStride = targetData.pixelStride;
        final int targetLineStride = targetData.lineStride;
        final short[] targetDataArray = targetData.getShortData(0);

        final long[] indexCache = new long[w];
        final short[] valueCache = new short[w];

        int targetLineOffset = targetData.getOffset(0);
        final AffineTransform i2m = model.getImageToModelTransform(getLevel());
        final Point2D point = new Point2D.Double();

        for (int y = 0; y < h; ++y) {
            int targetPixelOffset = targetLineOffset;

            pixelCounter.countPixels(targetData.rect.y + y);
            if (pixelCounter.leading > 0) {
                Arrays.fill(targetDataArray, targetPixelOffset, targetPixelOffset + pixelCounter.leading, noDataValue);
                targetPixelOffset += pixelCounter.leading * targetPixelStride;
            }
            if (pixelCounter.valid > 0) {
                for (int x = pixelCounter.leading; x < pixelCounter.leading + pixelCounter.valid; ++x) {
                    short value;
                    final long cellIndex = computeCellIndex(targetData.rect.x + x, targetData.rect.y + y, point);

                    if (x > 0 && indexCache[x - 1] == cellIndex) {
                        // pixel to the west
                        value = valueCache[x - 1];
                    } else if (y > 0 && indexCache[x] == cellIndex) {
                        // pixel to the north
                        value = valueCache[x];
                    } else if (x + 1 < w && y > 0 && indexCache[x + 1] == cellIndex) {
                        // pixel to the north-east
                        value = valueCache[x + 1];
                    } else {
                        if (cellIndex != -1) {
                            value = valueProvider.getLaiValue(cellIndex, noDataValue);
                        } else {
                            value = noDataValue;
                        }
                    }
                    indexCache[x] = cellIndex;
                    valueCache[x] = value;

                    targetDataArray[targetPixelOffset] = value;
                    targetPixelOffset += targetPixelStride;
                }
            }
            if (pixelCounter.trailing > 0) {
                Arrays.fill(targetDataArray, targetPixelOffset, targetPixelOffset + pixelCounter.trailing, noDataValue);
            }
            targetLineOffset += targetLineStride;
        }
    }

    private void intLoop(UnpackedImageData targetData, PixelCounter pixelCounter, int noDataValue) {
        final int w = targetData.rect.width;
        final int h = targetData.rect.height;

        final int targetPixelStride = targetData.pixelStride;
        final int targetLineStride = targetData.lineStride;
        final int[] targetDataArray = targetData.getIntData(0);

        final long[] indexCache = new long[w];
        final int[] valueCache = new int[w];

        int targetLineOffset = targetData.getOffset(0);
        final Point2D point = new Point2D.Double();

        for (int y = 0; y < h; ++y) {
            int targetPixelOffset = targetLineOffset;

            pixelCounter.countPixels(targetData.rect.y + y);
            if (pixelCounter.leading > 0) {
                Arrays.fill(targetDataArray, targetPixelOffset, targetPixelOffset + pixelCounter.leading, noDataValue);
                targetPixelOffset += pixelCounter.leading * targetPixelStride;
            }
            if (pixelCounter.valid > 0) {
                for (int x = pixelCounter.leading; x < pixelCounter.leading + pixelCounter.valid; ++x) {
                    int value;
                    final long cellIndex = computeCellIndex(targetData.rect.x + x, targetData.rect.y + y, point);

                    if (x > 0 && indexCache[x - 1] == cellIndex) {
                        // pixel to the west
                        value = valueCache[x - 1];
                    } else if (y > 0 && indexCache[x] == cellIndex) {
                        // pixel to the north
                        value = valueCache[x];
                    } else if (x + 1 < w && y > 0 && indexCache[x + 1] == cellIndex) {
                        // pixel to the north-east
                        value = valueCache[x + 1];
                    } else {
                        if (cellIndex != -1) {
                            value = valueProvider.getLaiValue(cellIndex, noDataValue);
                        } else {
                            value = noDataValue;
                        }
                    }
                    indexCache[x] = cellIndex;
                    valueCache[x] = value;

                    targetDataArray[targetPixelOffset] = value;
                    targetPixelOffset += targetPixelStride;
                }
            }
            if (pixelCounter.trailing > 0) {
                Arrays.fill(targetDataArray, targetPixelOffset, targetPixelOffset + pixelCounter.trailing, noDataValue);
            }
            targetLineOffset += targetLineStride;
        }
    }

    private void floatLoop(UnpackedImageData targetData, PixelCounter pixelCounter, float noDataValue) {
        final int w = targetData.rect.width;
        final int h = targetData.rect.height;

        final int targetPixelStride = targetData.pixelStride;
        final int targetLineStride = targetData.lineStride;
        final float[] targetDataArray = targetData.getFloatData(0);

        final long[] indexCache = new long[w];
        final float[] valueCache = new float[w];

        int targetLineOffset = targetData.getOffset(0);
        final AffineTransform i2m = model.getImageToModelTransform(getLevel());
        final Point2D point = new Point2D.Double();

        for (int y = 0; y < h; ++y) {
            int targetPixelOffset = targetLineOffset;

            pixelCounter.countPixels(targetData.rect.y + y);
            if (pixelCounter.leading > 0) {
                Arrays.fill(targetDataArray, targetPixelOffset, targetPixelOffset + pixelCounter.leading, noDataValue);
                targetPixelOffset += pixelCounter.leading * targetPixelStride;
            }
            if (pixelCounter.valid > 0) {
                for (int x = pixelCounter.leading; x < pixelCounter.leading + pixelCounter.valid; ++x) {
                    float value;
                    final long cellIndex = computeCellIndex(targetData.rect.x + x, targetData.rect.y + y, point);

                    if (x > 0 && indexCache[x - 1] == cellIndex) {
                        // pixel to the west
                        value = valueCache[x - 1];
                    } else if (y > 0 && indexCache[x] == cellIndex) {
                        // pixel to the north
                        value = valueCache[x];
                    } else if (x + 1 < w && y > 0 && indexCache[x + 1] == cellIndex) {
                        // pixel to the north-east
                        value = valueCache[x + 1];
                    } else {
                        if (cellIndex != -1) {
                            value = valueProvider.getLaiValue(cellIndex, noDataValue);
                        } else {
                            value = noDataValue;
                        }
                    }
                    indexCache[x] = cellIndex;
                    valueCache[x] = value;

                    targetDataArray[targetPixelOffset] = value;
                    targetPixelOffset += targetPixelStride;
                }
            }
            if (pixelCounter.trailing > 0) {
                Arrays.fill(targetDataArray, targetPixelOffset, targetPixelOffset + pixelCounter.trailing, noDataValue);
            }
            targetLineOffset += targetLineStride;
        }
    }

    private long computeCellIndex(int x, int y, Point2D point) {
        point.setLocation(x, y);
        imageToModelTransform.transform(point, point);

        final double lon = point.getX();
        final double lat = point.getY();

        return valueProvider.getCellIndex(lon, lat);
    }

    private static class PixelCounter {

        private final Rectangle effectiveBounds;
        private final Rectangle targetRectangle;

        private int leading;
        private int valid;
        private int trailing;

        PixelCounter(Rectangle targetRectangle, Area envelope) {
            final Area effectiveEnvelope = new Area(targetRectangle);
            effectiveEnvelope.intersect(envelope);

            this.effectiveBounds = effectiveEnvelope.getBounds();
            this.targetRectangle = targetRectangle;
        }

        void countPixels(int y) {
            if (y < effectiveBounds.y || y > effectiveBounds.y + effectiveBounds.height) {
                leading = targetRectangle.width;
                valid = 0;
                trailing = 0;
            } else {
                leading = effectiveBounds.x - targetRectangle.x;
                valid = effectiveBounds.width;
                trailing = targetRectangle.width - leading - valid;
            }
        }
    }

}
