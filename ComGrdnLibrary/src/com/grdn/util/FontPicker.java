package com.grdn.util;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.TextView;

public class FontPicker
{
	private static final String TAG = FontPicker.class.getSimpleName();
	public static final int FONT_FILTER_CLASS0 = 0;
	public static final int FONT_FILTER_CLASS1 = 32;
	public static final int FONT_FILTER_CLASS2 = 64;
	public static final int FONT_FILTER_CLASS3 = 96;
	public static final int FONT_FILTER_CLASS4 = 128;
	public static final int FONT_FILTER_CLASS5 = 160;
	public static final int FONT_FILTER_CLASS6 = 192;
	public static final int FONT_FILTER_CLASS7 = 224;
	public static final int FONT_SIZE_TYPE_0 = 0;
	public static final int FONT_SIZE_TYPE_1 = 1;
	private int mWidth;
	private int mHeight;
	private int mFontSizeType;
	private int mFontColor;
	private String mSrc;
	private Bitmap mBitmap;
	private TextView mTextView;

	public FontPicker(Context context)
	{
		mWidth = 0;
		mHeight = 0;
		mFontSizeType = 1;
		mFontColor = -1;
		mSrc = null;
		mBitmap = null;
		mTextView = null;
		mTextView = new TextView(context);
		mTextView.setTypeface(Typeface.defaultFromStyle(1));
		mTextView.setTextScaleX(0.76F);
		mTextView.setTextColor(mFontColor);
		mTextView.setMaxWidth(72);
		mTextView.setSingleLine();
	}

	public void setSrc(String src, int type)
	{
		if (src == null) {
			Log.e(TAG, "Parameter 'src' should not be null!");
		}
		mSrc = src;
		mFontSizeType = type;
		mTextView.setText(mSrc);
		mTextView.setTextSize(0, mFontSizeType != FONT_SIZE_TYPE_1 ? 18 : 24);
		mTextView.setDrawingCacheEnabled(true);
		mTextView.measure(android.view.View.MeasureSpec.makeMeasureSpec(0, 0), android.view.View.MeasureSpec.makeMeasureSpec(0, 0));
		mTextView.layout(0, 0, mTextView.getMeasuredWidth(), mTextView.getMeasuredHeight());
		mTextView.setGravity(80);
		mBitmap = mTextView.getDrawingCache();
		mWidth = mBitmap.getWidth();
		mHeight = mBitmap.getHeight();
		
		Log.e(TAG, (new StringBuilder("mWidth: ")).append(mWidth).toString());
		Log.e(TAG, (new StringBuilder("mHeight: ")).append(mHeight).toString());
		
		Drawable drawable = new BitmapDrawable(null, mBitmap);
		mBitmap = drawable2Bitmap(drawable, mWidth, mHeight);
		mHeight = mFontSizeType != FONT_SIZE_TYPE_1 ? 16 : 24;
	}

	public byte[] getFont(int filter)
	{
		int pixels[] = new int[mWidth * mHeight];
		byte result[] = new byte[(mWidth * mHeight)/8];
		mBitmap.getPixels(pixels, 0, mWidth, 0, mFontSizeType != FONT_SIZE_TYPE_1 ? 4 : 4, mWidth, mHeight);
		int i = 0;
		int n = 0;
		short data = 0;
		for (int x = 0; x < mWidth; x++)
		{
			String pixelsW = "";
			for (int y = mHeight - 1; y >= 0; y--)
			{
				int index = y * mWidth + x;
				int R = (pixels[index] & 0xff0000) >> 16;
				int G = (pixels[index] & 0xff00) >> 8;
				int B = (pixels[index] & 0xff) >> 0;
				int Y = (int)(0.299D * (double)R + 0.587 * (double)G + 0.114D * (double)B);
				pixelsW += String.format("%s", Y <= filter ? "O" : "I");
				if (n >= 8) {
					n = 0;
					result[i++] = (byte)data;
					data = 0;
				}
				if (Y > filter) {
					data |= 128 >> n;
				}
				n++;
			}

			Log.i(TAG, pixelsW);
		}

		return result;
	}

	public int getWidth()
	{
		if (mSrc == null) {
			Log.e(TAG, "'setSrc' must be called before!");
		}
		return mWidth;
	}

	public int getHeight()
	{
		if (mSrc == null) {
			Log.e(TAG, "'setSrc' must be called before!");
		}
		return mHeight;
	}

	private Bitmap drawable2Bitmap(Drawable drawable, int width, int height)
	{
		Bitmap bitmap = Bitmap.createBitmap(width, width, android.graphics.Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);
		return bitmap;
	}

}