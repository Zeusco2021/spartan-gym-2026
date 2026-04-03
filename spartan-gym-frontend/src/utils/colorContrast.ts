/**
 * WCAG 2.1 AA color contrast utilities.
 *
 * Minimum contrast ratios:
 *  - Normal text (< 18pt / < 14pt bold): 4.5:1
 *  - Large text  (≥ 18pt / ≥ 14pt bold): 3:1
 */

/**
 * Parse a color string (hex #RGB/#RRGGBB, rgb(), or rgba()) into [r, g, b] (0-255).
 */
export function hexToRgb(color: string): [number, number, number] {
  // Handle rgb(r, g, b) and rgba(r, g, b, a)
  const rgbMatch = color.match(
    /^rgba?\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)/,
  );
  if (rgbMatch) {
    return [Number(rgbMatch[1]), Number(rgbMatch[2]), Number(rgbMatch[3])];
  }

  const cleaned = color.replace('#', '');
  if (cleaned.length === 3) {
    const r = parseInt(cleaned[0] + cleaned[0], 16);
    const g = parseInt(cleaned[1] + cleaned[1], 16);
    const b = parseInt(cleaned[2] + cleaned[2], 16);
    return [r, g, b];
  }
  if (cleaned.length === 6) {
    return [
      parseInt(cleaned.slice(0, 2), 16),
      parseInt(cleaned.slice(2, 4), 16),
      parseInt(cleaned.slice(4, 6), 16),
    ];
  }
  throw new Error(`Invalid color: ${color}`);
}

/**
 * Compute the relative luminance of an sRGB color per WCAG 2.1.
 * https://www.w3.org/TR/WCAG21/#dfn-relative-luminance
 */
export function relativeLuminance(hex: string): number {
  const [r, g, b] = hexToRgb(hex).map((c) => {
    const s = c / 255;
    return s <= 0.04045 ? s / 12.92 : Math.pow((s + 0.055) / 1.055, 2.4);
  });
  return 0.2126 * r + 0.7152 * g + 0.0722 * b;
}

/**
 * Compute the contrast ratio between two colors (returns value ≥ 1).
 * https://www.w3.org/TR/WCAG21/#dfn-contrast-ratio
 */
export function contrastRatio(hex1: string, hex2: string): number {
  const l1 = relativeLuminance(hex1);
  const l2 = relativeLuminance(hex2);
  const lighter = Math.max(l1, l2);
  const darker = Math.min(l1, l2);
  return (lighter + 0.05) / (darker + 0.05);
}

/** WCAG 2.1 AA minimum ratio for normal text. */
export const WCAG_AA_NORMAL = 4.5;

/** WCAG 2.1 AA minimum ratio for large text (≥ 18pt or ≥ 14pt bold). */
export const WCAG_AA_LARGE = 3;

/**
 * Check whether two colors meet WCAG 2.1 AA for normal text.
 */
export function meetsWcagAANormal(fg: string, bg: string): boolean {
  return contrastRatio(fg, bg) >= WCAG_AA_NORMAL;
}

/**
 * Check whether two colors meet WCAG 2.1 AA for large text.
 */
export function meetsWcagAALarge(fg: string, bg: string): boolean {
  return contrastRatio(fg, bg) >= WCAG_AA_LARGE;
}
