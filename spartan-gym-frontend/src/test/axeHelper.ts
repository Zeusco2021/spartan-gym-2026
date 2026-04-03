/**
 * axe-core accessibility test helper for use in component tests.
 *
 * Usage:
 *   import { checkAccessibility } from '@/test/axeHelper';
 *
 *   it('should have no accessibility violations', async () => {
 *     const { container } = render(<MyComponent />);
 *     await checkAccessibility(container);
 *   });
 *
 * CI/CD integration:
 *   The `test` script in package.json (`vitest --run`) already executes
 *   all *.test.ts(x) files, including accessibility tests that use this
 *   helper. Add `npm test` (or `npx vitest --run`) as a step in your
 *   CI/CD pipeline to enforce accessibility checks on every build.
 */
import axe, { type RunOptions, type AxeResults } from 'axe-core';

const DEFAULT_AXE_OPTIONS: RunOptions = {
  runOnly: {
    type: 'tag',
    values: ['wcag2a', 'wcag2aa', 'best-practice'],
  },
};

/**
 * Run axe-core against a DOM container and fail the test if any
 * WCAG 2.1 AA violations are found.
 */
export async function checkAccessibility(
  container: HTMLElement,
  options: RunOptions = DEFAULT_AXE_OPTIONS,
): Promise<AxeResults> {
  const results = await axe.run(container, options);

  if (results.violations.length > 0) {
    const messages = results.violations.map(
      (v) =>
        `[${v.impact}] ${v.id}: ${v.description}\n` +
        v.nodes.map((n) => `  - ${n.html}\n    ${n.failureSummary}`).join('\n'),
    );
    throw new Error(
      `Accessibility violations found:\n\n${messages.join('\n\n')}`,
    );
  }

  return results;
}
