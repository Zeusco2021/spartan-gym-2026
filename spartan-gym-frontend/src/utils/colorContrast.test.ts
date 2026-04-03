import { describe, it, expect } from 'vitest';
import {
  hexToRgb,
  relativeLuminance,
  contrastRatio,
  meetsWcagAANormal,
  meetsWcagAALarge,
} from './colorContrast';

describe('colorContrast utilities', () => {
  describe('hexToRgb', () => {
    it('parses 6-digit hex', () => {
      expect(hexToRgb('#FFFFFF')).toEqual([255, 255, 255]);
      expect(hexToRgb('#000000')).toEqual([0, 0, 0]);
      expect(hexToRgb('#D4AF37')).toEqual([212, 175, 55]);
    });

    it('parses 3-digit shorthand hex', () => {
      expect(hexToRgb('#FFF')).toEqual([255, 255, 255]);
      expect(hexToRgb('#000')).toEqual([0, 0, 0]);
    });

    it('throws on invalid color', () => {
      expect(() => hexToRgb('#GG')).toThrow('Invalid color');
    });
  });

  describe('hexToRgb with rgba', () => {
    it('parses rgb()', () => {
      expect(hexToRgb('rgb(0, 0, 0)')).toEqual([0, 0, 0]);
      expect(hexToRgb('rgb(255, 255, 255)')).toEqual([255, 255, 255]);
    });

    it('parses rgba()', () => {
      expect(hexToRgb('rgba(0, 0, 0, 0.87)')).toEqual([0, 0, 0]);
    });
  });

  describe('relativeLuminance', () => {
    it('returns 1 for white', () => {
      expect(relativeLuminance('#FFFFFF')).toBeCloseTo(1, 4);
    });

    it('returns 0 for black', () => {
      expect(relativeLuminance('#000000')).toBeCloseTo(0, 4);
    });
  });

  describe('contrastRatio', () => {
    it('returns 21:1 for black on white', () => {
      expect(contrastRatio('#000000', '#FFFFFF')).toBeCloseTo(21, 0);
    });

    it('returns 1:1 for same color', () => {
      expect(contrastRatio('#D4AF37', '#D4AF37')).toBeCloseTo(1, 2);
    });

    it('is symmetric', () => {
      const r1 = contrastRatio('#D4AF37', '#FFFFFF');
      const r2 = contrastRatio('#FFFFFF', '#D4AF37');
      expect(r1).toBeCloseTo(r2, 4);
    });
  });

  describe('meetsWcagAANormal', () => {
    it('black on white passes (21:1)', () => {
      expect(meetsWcagAANormal('#000000', '#FFFFFF')).toBe(true);
    });

    it('same color fails (1:1)', () => {
      expect(meetsWcagAANormal('#888888', '#888888')).toBe(false);
    });
  });

  describe('meetsWcagAALarge', () => {
    it('black on white passes', () => {
      expect(meetsWcagAALarge('#000000', '#FFFFFF')).toBe(true);
    });
  });
});
