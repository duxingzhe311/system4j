package com.dw.system.util;

import java.awt.*;

import javax.swing.plaf.*;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalTheme;

public class DWMetalTheme extends MetalTheme
{
	/**
	 * Names of the fonts to use.
	 */
	private static final String[] fontNames = {
		"Dialog", "Dialog", "Dialog", "Dialog", "Dialog", "Dialog"
	};
	/**
	 * Styles for the fonts.
	 */
	private static final int[] fontStyles = {
		Font.PLAIN, Font.PLAIN, Font.PLAIN, Font.PLAIN, Font.PLAIN, Font.PLAIN
	};
	/**
	 * Sizes for the fonts.
	 */
	private static final int[] fontSizes = {
		12, 12, 12, 12, 12, 10
	};

	// note the properties listed here can currently be used by people
	// providing runtimes to hint what fonts are good.  For example the bold
	// dialog font looks bad on a Mac, so Apple could use this property to
	// hint at a good font.
	//
	// However, we don't promise to support these forever.  We may move
	// to getting these from the swing.properties file, or elsewhere.
	/**
	 * System property names used to look up fonts.
	 */
	private static final String[] defaultNames = {
		"swing.plaf.metal.controlFont",
		"swing.plaf.metal.systemFont",
		"swing.plaf.metal.userFont",
		"swing.plaf.metal.controlFont",
		"swing.plaf.metal.controlFont",
		"swing.plaf.metal.smallFont"
	};

	/**
	 * Returns the ideal font name for the font identified by key.
	 */
	static String getDefaultFontName(int key) {
		return fontNames[key];
	}

	/**
	 * Returns the ideal font size for the font identified by key.
	 */
	static int getDefaultFontSize(int key) {
		return fontSizes[key];
	}

	/**
	 * Returns the ideal font style for the font identified by key.
	 */
	static int getDefaultFontStyle(int key) {
		return fontStyles[key];
	}

	/**
	 * Returns the default used to look up the specified font.
	 */
	static String getDefaultPropertyName(int key) {
		return defaultNames[key];
	}

	private static final ColorUIResource primary1 = new ColorUIResource(
							  102, 102, 153);
	private static final ColorUIResource primary2 = new ColorUIResource(153,
							  153, 204);
	private static final ColorUIResource primary3 = new ColorUIResource(
							  204, 204, 255);
	private static final ColorUIResource secondary1 = new ColorUIResource(
							  102, 102, 102);
	private static final ColorUIResource secondary2 = new ColorUIResource(
							  153, 153, 153);
	private static final ColorUIResource secondary3 = new ColorUIResource(
							  204, 204, 204);

	private FontDelegate fontDelegate;

	public String getName() { return "Steel"; }

	public DWMetalTheme() {
		install();
	}

	// these are blue in Metal Default Theme
	protected ColorUIResource getPrimary1() { return primary1; }
	protected ColorUIResource getPrimary2() { return primary2; }
	protected ColorUIResource getPrimary3() { return primary3; }

	// these are gray in Metal Default Theme
	protected ColorUIResource getSecondary1() { return secondary1; }
	protected ColorUIResource getSecondary2() { return secondary2; }
	protected ColorUIResource getSecondary3() { return secondary3; }

	private FontUIResource fontRs = new FontUIResource("ËÎÌו", FontUIResource.PLAIN, 12);

	public FontUIResource getControlTextFont() {
		return fontRs;
	}

	public FontUIResource getSystemTextFont() {
		return fontRs;
	}

	public FontUIResource getUserTextFont() {
		return fontRs;
	}

	public FontUIResource getMenuTextFont() {
		return fontRs;
	}

	public FontUIResource getWindowTitleFont() {
		return fontRs;
	}

	public FontUIResource getSubTextFont() {
		return fontRs;
	}

	private FontUIResource getFont(int key) {
		return fontDelegate.getFont(key);
	}

	void install() {
		fontDelegate = new FontDelegate();
	}

	/**
	 * Returns true if this is a theme provided by the core platform.
	 */
	boolean isSystemTheme() {
		return getClass().equals(DefaultMetalTheme.class);
	}

	/**
	 * FontDelegates add an extra level of indirection to obtaining fonts.
	 */
	private static class FontDelegate {
		FontUIResource fonts[];

		// menu and window are mapped to controlFont
		public FontDelegate() {
			fonts = new FontUIResource[6];
		}

		public FontUIResource getFont(int type) {
			return new FontUIResource("ËÎÌו", FontUIResource.PLAIN, 12);
		}

		/**
		 * This is the same as invoking
		 * <code>Font.getFont(key)</code>, with the exception
		 * that it is wrapped inside a <code>doPrivileged</code> call.
		 */
		protected Font getPrivilegedFont(final int key) {
			return (Font)java.security.AccessController.doPrivileged(
				new java.security.PrivilegedAction() {
					public Object run() {
						return Font.getFont(getDefaultPropertyName(key));
					}
				}
				);
		}
	}

}
