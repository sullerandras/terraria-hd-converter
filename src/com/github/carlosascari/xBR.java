package com.github.carlosascari;

/**
 * 2xBR Filter
 *
 * Java port of the javascript implementation of the 2xBR filter.
 *
 * This is a rewrite of the previous 0.2.5 version, it outputs the same quality,
 * however this version is about a magnitude **slower** than its predecessor.
 *
 * Use this version if you want to learn how the algorithms works, as the code is
 * much more readable.
 *
 * @version 0.3.0
 * @author Ascari <carlos.ascari.x@gmail.com>
 */
public class xBR {
	// 2xBR
	public static int SCALE = 2;

	// Weights should emphasize luminance (Y), in order to work. Feel free to experiment.
	public static int Y_WEIGHT = 48;
	public static int U_WEIGHT = 7;
	public static int V_WEIGHT = 6;

	// -----------------------------------------------------------------------------

	/**
	* Returns the absolute value of a number.
	*
	* **Note**
	* `return (x >> 31) ^ x + (x >> 31)` also works (w/out a mask)
	*
	* @method abs
	* @param x {Number}
	* @return Number
	*/
	//function abs(x)
	//{
	//	var mask = x >> 31
	//	x = x ^ mask
	//	x = x - mask
	//	return x
	//}

	/**
	* Calculates the weighted difference between two pixels.
	*
	* These are the steps:
	*
	* 1. Finds absolute color difference between two pixels.
	* 2. Converts color difference into Y'UV, separating color from light.
	* 3. Applies Y'UV thresholds, giving importance to luminance.
	*
	* @method d
	* @param pixelA {Pixel}
	* @param pixelB {Pixel}
	* @return Number
	*/
	private static float d(Pixel pixelA, Pixel pixelB) {
		final int r = Math.abs(pixelA.red() - pixelB.red());
		final int b = Math.abs(pixelA.blue() - pixelB.blue());
		final int g = Math.abs(pixelA.green() - pixelB.green());
		final float y = r *  .299000f + g *  .587000f + b *  .114000f;
		final float u = r * -.168736f + g * -.331264f + b *  .500000f;
		final float v = r *  .500000f + g * -.418688f + b * -.081312f;
		final float weight = (y * Y_WEIGHT) + (u * U_WEIGHT ) + (v * V_WEIGHT);
		return weight;
	}

	/**
	* Blends two pixels together and returns an new Pixel.
	*
	* **Note** This function ignores the alpha channel, if you wanted to work on
	* images with transparency, this is where you'd want to start.
	*
	* @method blend
	* @param pixelA {Pixel}
	* @param pixelB {Pixel}
	* @param alpha {Number}
	* @return Pixel
	*/
	private static Pixel blend(Pixel pixelA, Pixel pixelB, float alpha) {
		if (pixelA.alpha() == 0 && pixelB.alpha() == 0) {
			return new Pixel(0);
		}
		final double reverseAlpha = 1 - alpha;
		final int r = (int) Math.round((alpha * pixelB.red())   + (reverseAlpha * pixelA.red()));
		final int g = (int) Math.round((alpha * pixelB.green()) + (reverseAlpha * pixelA.green()));
		final int b = (int) Math.round((alpha * pixelB.blue())  + (reverseAlpha * pixelA.blue()));
		return new Pixel(r, g, b, Math.max(pixelA.alpha(), pixelB.alpha()));
	}

	// -----------------------------------------------------------------------------

	/**
	* Converts x,y coordinates into an index pointing to the same pixel
	* in a Uint32Array.
	*
	* @method coord2index
	* @param x {Number}
	* @param y {Number}
	* @return Number
	*/
	private static final int coord2index(final int x, final int y, final int srcW) {
		return srcW * y + x;
	}

	private static final int getPixelAt(final int[] array, final int x, final int y, final int width, final int height) {
		if (x < 0 || y < 0 || x >= width || y >= height) {
			return 0;
		}
		return array[coord2index(x, y, width)];
	}

	/**
	* Applies the xBR filter.
	*
	* @method execute
	* @return ImageData
	*/
	public static void execute(int[] oPixelView, int[] sPixelView, int srcW, int srcH) {
		if (oPixelView.length != srcW * srcH) {
			throw new IllegalArgumentException("oPixelView.length <> srcW * srcH: "+oPixelView.length+" <> "+srcW+" * "+srcH);
		}
		// scaled
		final int scaledWidth = srcW * SCALE;
		final int scaledHeight = srcH * SCALE;
		if (sPixelView.length != scaledWidth * scaledHeight) {
			throw new IllegalArgumentException("sPixelView.length <> scaledWidth * scaledHeight: "+sPixelView.length+" <> "+scaledWidth+" * "+scaledHeight);
		}

		/**
		 * This is the window or `vision` of the xBR algorithm. The 10th index, the pixel
		 * at the center holds the current pixel being scaled.
		 *
		 * @property matrix
		 * @type Array
		 */
		final Pixel[] matrix = new Pixel[21];

		/*
		* Main Loop; Algorithm is applied here
		*/
		for (int x = 0; x < srcW; ++x) {
			for (int y = 0; y < srcH; ++y) {
				/* Matrix: 10 is (0,0) i.e. current pixel.
					-2 | -1|  0| +1| +2 	(x)
				______________________________
				-2 |	    [ 0][ 1][ 2]
				-1 |	[ 3][ 4][ 5][ 6][ 7]
				 0 |	[ 8][ 9][10][11][12]
				+1 |	[13][14][15][16][17]
				+2 |	    [18][19][20]
				(y)|
				*/
				matrix[ 0] = new Pixel(getPixelAt(oPixelView, x-1, y-2, srcW, srcH));
				matrix[ 1] = new Pixel(getPixelAt(oPixelView,   x, y-2, srcW, srcH));
				matrix[ 2] = new Pixel(getPixelAt(oPixelView, x+1, y-2, srcW, srcH));
				matrix[ 3] = new Pixel(getPixelAt(oPixelView, x-2, y-1, srcW, srcH));
				matrix[ 4] = new Pixel(getPixelAt(oPixelView, x-1, y-1, srcW, srcH));
				matrix[ 5] = new Pixel(getPixelAt(oPixelView,   x, y-1, srcW, srcH));
				matrix[ 6] = new Pixel(getPixelAt(oPixelView, x+1, y-1, srcW, srcH));
				matrix[ 7] = new Pixel(getPixelAt(oPixelView, x+2, y-1, srcW, srcH));
				matrix[ 8] = new Pixel(getPixelAt(oPixelView, x-2,   y, srcW, srcH));
				matrix[ 9] = new Pixel(getPixelAt(oPixelView, x-1,   y, srcW, srcH));
				matrix[10] = new Pixel(getPixelAt(oPixelView,   x,   y, srcW, srcH));
				matrix[11] = new Pixel(getPixelAt(oPixelView, x+1,   y, srcW, srcH));
				matrix[12] = new Pixel(getPixelAt(oPixelView, x+2,   y, srcW, srcH));
				matrix[13] = new Pixel(getPixelAt(oPixelView, x-2, y+1, srcW, srcH));
				matrix[14] = new Pixel(getPixelAt(oPixelView, x-1, y+1, srcW, srcH));
				matrix[15] = new Pixel(getPixelAt(oPixelView,   x, y+1, srcW, srcH));
				matrix[16] = new Pixel(getPixelAt(oPixelView, x+1, y+1, srcW, srcH));
				matrix[17] = new Pixel(getPixelAt(oPixelView, x+2, y+1, srcW, srcH));
				matrix[18] = new Pixel(getPixelAt(oPixelView, x-1, y+2, srcW, srcH));
				matrix[19] = new Pixel(getPixelAt(oPixelView,   x, y+2, srcW, srcH));
				matrix[20] = new Pixel(getPixelAt(oPixelView, x+1, y+2, srcW, srcH));

				// Calculate color weights using 2 points in the matrix
				final float d_10_9  = d(matrix[10], matrix[9]);
				final float d_10_5  = d(matrix[10], matrix[5]);
				final float d_10_11 = d(matrix[10], matrix[11]);
				final float d_10_15 = d(matrix[10], matrix[15]);
				final float d_10_14 = d(matrix[10], matrix[14]);
				final float d_10_6  = d(matrix[10], matrix[6]);
				final float d_4_8   = d(matrix[4],  matrix[8]);
				final float d_4_1   = d(matrix[4],  matrix[1]);
				final float d_9_5   = d(matrix[9],  matrix[5]);
				final float d_9_15  = d(matrix[9],  matrix[15]);
				final float d_9_3   = d(matrix[9],  matrix[3]);
				final float d_5_11  = d(matrix[5],  matrix[11]);
				final float d_5_0   = d(matrix[5],  matrix[0]);
				final float d_10_4  = d(matrix[10], matrix[4]);
				final float d_10_16 = d(matrix[10], matrix[16]);
				final float d_6_12  = d(matrix[6],  matrix[12]);
				final float d_6_1   = d(matrix[6],  matrix[1]);
				final float d_11_15 = d(matrix[11], matrix[15]);
				final float d_11_7  = d(matrix[11], matrix[7]);
				final float d_5_2   = d(matrix[5],  matrix[2]);
				final float d_14_8  = d(matrix[14], matrix[8]);
				final float d_14_19 = d(matrix[14], matrix[19]);
				final float d_15_18 = d(matrix[15], matrix[18]);
				final float d_9_13  = d(matrix[9],  matrix[13]);
				final float d_16_12 = d(matrix[16], matrix[12]);
				final float d_16_19 = d(matrix[16], matrix[19]);
				final float d_15_20 = d(matrix[15], matrix[20]);
				final float d_15_17 = d(matrix[15], matrix[17]);

				// Top Left Edge Detection Rule
				final float a1 = (d_10_14 + d_10_6 + d_4_8  + d_4_1 + (4 * d_9_5));
				final float b1 = ( d_9_15 +  d_9_3 + d_5_11 + d_5_0 + (4 * d_10_4));
				if (a1 < b1) {
					final Pixel new_pixel = (d_10_9 <= d_10_5) ? matrix[9] : matrix[5];
					final Pixel blended_pixel = blend(new_pixel, matrix[10], 0.5f);
					sPixelView[((y * SCALE) * scaledWidth) + (x * SCALE)] = blended_pixel.getValue();
				} else {
					sPixelView[((y * SCALE) * scaledWidth) + (x * SCALE)] = matrix[10].getValue();
				}

				// Top Right Edge Detection Rule
				final float a2 = (d_10_16 + d_10_4 + d_6_12 + d_6_1 + (4 * d_5_11));
				final float b2 = (d_11_15 + d_11_7 +  d_9_5 + d_5_2 + (4 * d_10_6));
				if (a2 < b2) {
					final Pixel new_pixel= (d_10_5 <= d_10_11) ? matrix[5] : matrix[11];
					final Pixel blended_pixel = blend(new_pixel, matrix[10], 0.5f);
					sPixelView[((y * SCALE) * scaledWidth) + (x * SCALE + 1)] = blended_pixel.getValue();
				} else {
					sPixelView[((y * SCALE) * scaledWidth) + (x * SCALE + 1)] = matrix[10].getValue();
				}

				// Bottom Left Edge Detection Rule
				final float a3 = (d_10_4 + d_10_16 +  d_14_8 + d_14_19 + (4 * d_9_15));
				final float b3 = ( d_9_5 +  d_9_13 + d_11_15 + d_15_18 + (4 * d_10_14));
				if (a3 < b3) {
					final Pixel new_pixel= (d_10_9 <= d_10_15) ? matrix[9] : matrix[15];
					final Pixel blended_pixel = blend(new_pixel, matrix[10], 0.5f);
					final int index = ((y * SCALE + 1) * scaledWidth) + (x * SCALE);
					sPixelView[index] = blended_pixel.getValue();
				} else {
					final int index = ((y * SCALE + 1) * scaledWidth) + (x * SCALE);
					sPixelView[index] = matrix[10].getValue();
				}

				// Bottom Right Edge Detection Rule
				final float a4 = (d_10_6 + d_10_14 + d_16_12 + d_16_19 + (4 * d_11_15));
				final float b4 = (d_9_15 + d_15_20 + d_15_17 +  d_5_11 + (4 * d_10_16));
				if (a4 < b4) {
					final Pixel new_pixel= (d_10_11 <= d_10_15) ? matrix[11] : matrix[15];
					final Pixel blended_pixel = blend(new_pixel, matrix[10], 0.5f);
					sPixelView[((y * SCALE + 1) * scaledWidth) + (x * SCALE + 1)] = blended_pixel.getValue();
				} else {
					sPixelView[((y * SCALE + 1) * scaledWidth) + (x * SCALE + 1)] = matrix[10].getValue();
				}
			}
		}
	}
}
