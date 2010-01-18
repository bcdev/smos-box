/* 
 * Copyright (C) 2002-2008 by Brockmann Consult
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.esa.beam.dataio.smos;

import com.bc.ceres.glevel.MultiLevelModel;
import com.bc.ceres.jai.NoDataRaster;
import org.esa.beam.framework.datamodel.RasterDataNode;
import org.esa.beam.jai.ImageManager;
import org.esa.beam.jai.ResolutionLevel;
import org.esa.beam.jai.SingleBandedOpImage;
import org.esa.beam.smos.dgg.SmosDgg;

import javax.media.jai.PixelAccessor;
import javax.media.jai.PlanarImage;
import javax.media.jai.UnpackedImageData;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;

class SmosOpImage extends SingleBandedOpImage {

    private final ValueProvider valueProvider;
    private final MultiLevelModel model;
    private final double noDataValue;

    private volatile Area area;
    private volatile NoDataRaster noDataRaster;

    SmosOpImage(ValueProvider valueProvider, RasterDataNode rasterDataNode, MultiLevelModel model,
                ResolutionLevel level) {
        super(ImageManager.getDataBufferType(rasterDataNode.getDataType()),
              rasterDataNode.getSceneRasterWidth(),
              rasterDataNode.getSceneRasterHeight(),
              rasterDataNode.getProduct().getPreferredTileSize(),
              null, // no configuration
              level);


        this.valueProvider = valueProvider;
        this.model = model;
        this.noDataValue = rasterDataNode.getNoDataValue();
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
        final RenderedImage seqnumImage = SmosDgg.getInstance().getDggMultiLevelImage().getImage(getLevel());
        final Raster seqnumRaster = seqnumImage.getData(rectangle);
        final ColorModel cm = getColorModel();

        final PixelAccessor seqnumAccessor = new PixelAccessor(seqnumRaster.getSampleModel(), cm);
        final PixelAccessor targetAccessor = new PixelAccessor(targetRaster.getSampleModel(), null);

        final UnpackedImageData seqnumData = seqnumAccessor.getPixels(
                seqnumRaster, rectangle, seqnumRaster.getSampleModel().getTransferType(), false);
        final UnpackedImageData targetData = targetAccessor.getPixels(
                targetRaster, rectangle, targetRaster.getSampleModel().getTransferType(), true);
        final PixelCounter pixelCounter = new PixelCounter(rectangle, getArea());

        switch (targetData.type) {
        case DataBuffer.TYPE_BYTE:
            byteLoop(seqnumData, targetData, pixelCounter, (byte) noDataValue);
            break;
        case DataBuffer.TYPE_SHORT:
        case DataBuffer.TYPE_USHORT:
            shortLoop(seqnumData, targetData, pixelCounter, (short) noDataValue);
            break;
        case DataBuffer.TYPE_INT:
            intLoop(seqnumData, targetData, pixelCounter, (int) noDataValue);
            break;
        case DataBuffer.TYPE_FLOAT:
            floatLoop(seqnumData, targetData, pixelCounter, (float) noDataValue);
            break;
        default:
            // do nothing
            break;
        }

        targetAccessor.setPixels(targetData);
    }

    private void byteLoop(UnpackedImageData seqnumData, UnpackedImageData targetData, PixelCounter pixelCounter,
                          byte noDataValue) {
        final int w = targetData.rect.width;
        final int h = targetData.rect.height;

        final int seqnumPixelStride = seqnumData.pixelStride;
        final int seqnumLineStride = seqnumData.lineStride;
        final int[] seqnumDataArray = seqnumData.getIntData(0);

        final int targetPixelStride = targetData.pixelStride;
        final int targetLineStride = targetData.lineStride;
        final byte[] targetDataArray = targetData.getByteData(0);

        final int[] seqnumCache = new int[w];
        final byte[] valueCache = new byte[w];

        int seqnumLineOffset = seqnumData.getOffset(0);
        int targetLineOffset = targetData.getOffset(0);

        for (int y = 0; y < h; ++y) {
            int seqnumPixelOffset = seqnumLineOffset;
            int targetPixelOffset = targetLineOffset;

            pixelCounter.countPixels(targetData.rect.y + y);
            if (pixelCounter.leading > 0) {
                Arrays.fill(targetDataArray, targetPixelOffset, targetPixelOffset + pixelCounter.leading, noDataValue);
                targetPixelOffset += pixelCounter.leading * targetPixelStride;
                seqnumPixelOffset += pixelCounter.leading * seqnumPixelStride;
            }
            if (pixelCounter.valid > 0) {
                for (int x = pixelCounter.leading; x < pixelCounter.leading + pixelCounter.valid; ++x) {
                    byte value;
                    final int seqnum = seqnumDataArray[seqnumPixelOffset];
                    if (x > 0 && seqnumCache[x - 1] == seqnum) {
                        // pixel to the west
                        value = valueCache[x - 1];
                    } else if (y > 0 && seqnumCache[x] == seqnum) {
                        // pixel to the north
                        value = valueCache[x];
                    } else if (x + 1 < w && y > 0 && seqnumCache[x + 1] == seqnum) {
                        // pixel to the north-east
                        value = valueCache[x + 1];
                    } else {
                        final int gridPointIndex = valueProvider.getGridPointIndex(seqnum);
                        if (gridPointIndex != -1) {
                            value = valueProvider.getValue(gridPointIndex, noDataValue);
                        } else {
                            value = noDataValue;
                        }
                    }
                    seqnumCache[x] = seqnum;
                    valueCache[x] = value;
                    targetDataArray[targetPixelOffset] = value;
                    seqnumPixelOffset += seqnumPixelStride;
                    targetPixelOffset += targetPixelStride;
                }
            }
            if (pixelCounter.trailing > 0) {
                Arrays.fill(targetDataArray, targetPixelOffset, targetPixelOffset + pixelCounter.trailing, noDataValue);
            }
            seqnumLineOffset += seqnumLineStride;
            targetLineOffset += targetLineStride;
        }
    }

    private void shortLoop(UnpackedImageData seqnumData, UnpackedImageData targetData, PixelCounter pixelCounter,
                           short noDataValue) {
        final int w = targetData.rect.width;
        final int h = targetData.rect.height;

        final int seqnumPixelStride = seqnumData.pixelStride;
        final int seqnumLineStride = seqnumData.lineStride;
        final int[] seqnumDataArray = seqnumData.getIntData(0);

        final int targetPixelStride = targetData.pixelStride;
        final int targetLineStride = targetData.lineStride;
        final short[] targetDataArray = targetData.getShortData(0);

        final int[] seqnumCache = new int[w];
        final short[] valueCache = new short[w];

        int seqnumLineOffset = seqnumData.getOffset(0);
        int targetLineOffset = targetData.getOffset(0);

        for (int y = 0; y < h; ++y) {
            int seqnumPixelOffset = seqnumLineOffset;
            int targetPixelOffset = targetLineOffset;

            pixelCounter.countPixels(targetData.rect.y + y);
            if (pixelCounter.leading > 0) {
                Arrays.fill(targetDataArray, targetPixelOffset, targetPixelOffset + pixelCounter.leading, noDataValue);
                targetPixelOffset += pixelCounter.leading * targetPixelStride;
                seqnumPixelOffset += pixelCounter.leading * seqnumPixelStride;
            }
            if (pixelCounter.valid > 0) {
                for (int x = pixelCounter.leading; x < pixelCounter.leading + pixelCounter.valid; ++x) {
                    short value;
                    final int seqnum = seqnumDataArray[seqnumPixelOffset];
                    if (x > 0 && seqnumCache[x - 1] == seqnum) {
                        // pixel to the west
                        value = valueCache[x - 1];
                    } else if (y > 0 && seqnumCache[x] == seqnum) {
                        // pixel to the north
                        value = valueCache[x];
                    } else if (x + 1 < w && y > 0 && seqnumCache[x + 1] == seqnum) {
                        // pixel to the north-east
                        value = valueCache[x + 1];
                    } else {
                        final int gridPointIndex = valueProvider.getGridPointIndex(seqnum);
                        if (gridPointIndex != -1) {
                            value = valueProvider.getValue(gridPointIndex, noDataValue);
                        } else {
                            value = noDataValue;
                        }
                    }
                    seqnumCache[x] = seqnum;
                    valueCache[x] = value;
                    targetDataArray[targetPixelOffset] = value;
                    seqnumPixelOffset += seqnumPixelStride;
                    targetPixelOffset += targetPixelStride;
                }
            }
            if (pixelCounter.trailing > 0) {
                Arrays.fill(targetDataArray, targetPixelOffset, targetPixelOffset + pixelCounter.trailing, noDataValue);
            }
            seqnumLineOffset += seqnumLineStride;
            targetLineOffset += targetLineStride;
        }
    }

    private void intLoop(UnpackedImageData seqnumData, UnpackedImageData targetData, PixelCounter pixelCounter,
                         int noDataValue) {
        final int w = targetData.rect.width;
        final int h = targetData.rect.height;

        final int seqnumPixelStride = seqnumData.pixelStride;
        final int seqnumLineStride = seqnumData.lineStride;
        final int[] seqnumDataArray = seqnumData.getIntData(0);

        final int targetPixelStride = targetData.pixelStride;
        final int targetLineStride = targetData.lineStride;
        final int[] targetDataArray = targetData.getIntData(0);

        final int[] seqnumCache = new int[w];
        final int[] valueCache = new int[w];

        int seqnumLineOffset = seqnumData.getOffset(0);
        int targetLineOffset = targetData.getOffset(0);

        for (int y = 0; y < h; ++y) {
            int seqnumPixelOffset = seqnumLineOffset;
            int targetPixelOffset = targetLineOffset;

            pixelCounter.countPixels(targetData.rect.y + y);
            if (pixelCounter.leading > 0) {
                Arrays.fill(targetDataArray, targetPixelOffset, targetPixelOffset + pixelCounter.leading, noDataValue);
                targetPixelOffset += pixelCounter.leading * targetPixelStride;
                seqnumPixelOffset += pixelCounter.leading * seqnumPixelStride;
            }
            if (pixelCounter.valid > 0) {
                for (int x = pixelCounter.leading; x < pixelCounter.leading + pixelCounter.valid; ++x) {
                    int value;
                    final int seqnum = seqnumDataArray[seqnumPixelOffset];
                    if (x > 0 && seqnumCache[x - 1] == seqnum) {
                        // pixel to the west
                        value = valueCache[x - 1];
                    } else if (y > 0 && seqnumCache[x] == seqnum) {
                        // pixel to the north
                        value = valueCache[x];
                    } else if (x + 1 < w && y > 0 && seqnumCache[x + 1] == seqnum) {
                        // pixel to the north-east
                        value = valueCache[x + 1];
                    } else {
                        final int gridPointIndex = valueProvider.getGridPointIndex(seqnum);
                        if (gridPointIndex != -1) {
                            value = valueProvider.getValue(gridPointIndex, noDataValue);
                        } else {
                            value = noDataValue;
                        }
                    }
                    seqnumCache[x] = seqnum;
                    valueCache[x] = value;
                    targetDataArray[targetPixelOffset] = value;
                    seqnumPixelOffset += seqnumPixelStride;
                    targetPixelOffset += targetPixelStride;
                }
            }
            if (pixelCounter.trailing > 0) {
                Arrays.fill(targetDataArray, targetPixelOffset, targetPixelOffset + pixelCounter.trailing, noDataValue);
            }
            seqnumLineOffset += seqnumLineStride;
            targetLineOffset += targetLineStride;
        }
    }

    private void floatLoop(UnpackedImageData seqnumData, UnpackedImageData targetData, PixelCounter pixelCounter,
                           float noDataValue) {
        final int w = targetData.rect.width;
        final int h = targetData.rect.height;

        final int seqnumPixelStride = seqnumData.pixelStride;
        final int seqnumLineStride = seqnumData.lineStride;
        final int[] seqnumDataArray = seqnumData.getIntData(0);

        final int targetPixelStride = targetData.pixelStride;
        final int targetLineStride = targetData.lineStride;
        final float[] targetDataArray = targetData.getFloatData(0);

        final int[] seqnumCache = new int[w];
        final float[] valueCache = new float[w];

        int seqnumLineOffset = seqnumData.getOffset(0);
        int targetLineOffset = targetData.getOffset(0);

        for (int y = 0; y < h; ++y) {
            int seqnumPixelOffset = seqnumLineOffset;
            int targetPixelOffset = targetLineOffset;

            pixelCounter.countPixels(targetData.rect.y + y);
            if (pixelCounter.leading > 0) {
                Arrays.fill(targetDataArray, targetPixelOffset, targetPixelOffset + pixelCounter.leading, noDataValue);
                targetPixelOffset += pixelCounter.leading * targetPixelStride;
                seqnumPixelOffset += pixelCounter.leading * seqnumPixelStride;
            }
            if (pixelCounter.valid > 0) {
                for (int x = pixelCounter.leading; x < pixelCounter.leading + pixelCounter.valid; ++x) {
                    float value;
                    final int seqnum = seqnumDataArray[seqnumPixelOffset];
                    if (x > 0 && seqnumCache[x - 1] == seqnum) {
                        // pixel to the west
                        value = valueCache[x - 1];
                    } else if (y > 0 && seqnumCache[x] == seqnum) {
                        // pixel to the north
                        value = valueCache[x];
                    } else if (x + 1 < w && y > 0 && seqnumCache[x + 1] == seqnum) {
                        // pixel to the north-east
                        value = valueCache[x + 1];
                    } else {
                        final int gridPointIndex = valueProvider.getGridPointIndex(seqnum);
                        if (gridPointIndex != -1) {
                            value = valueProvider.getValue(gridPointIndex, noDataValue);
                        } else {
                            value = noDataValue;
                        }
                    }
                    seqnumCache[x] = seqnum;
                    valueCache[x] = value;
                    targetDataArray[targetPixelOffset] = value;
                    seqnumPixelOffset += seqnumPixelStride;
                    targetPixelOffset += targetPixelStride;
                }
            }
            if (pixelCounter.trailing > 0) {
                Arrays.fill(targetDataArray, targetPixelOffset, targetPixelOffset + pixelCounter.trailing, noDataValue);
            }
            seqnumLineOffset += seqnumLineStride;
            targetLineOffset += targetLineStride;
        }
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
