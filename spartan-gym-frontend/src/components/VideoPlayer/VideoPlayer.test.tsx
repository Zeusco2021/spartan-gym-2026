import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import VideoPlayer from './VideoPlayer';

vi.mock('video.js', () => {
  const mockPlayer = {
    dispose: vi.fn(),
    isDisposed: vi.fn(() => false),
  };
  const videojs = vi.fn(() => mockPlayer);
  return { default: videojs };
});

describe('VideoPlayer', () => {
  it('renders with default aria-label', () => {
    render(<VideoPlayer src="https://example.com/video.m3u8" />);

    expect(screen.getByRole('region', { name: 'Video player' })).toBeInTheDocument();
  });

  it('renders with custom aria-label', () => {
    render(
      <VideoPlayer
        src="https://example.com/video.m3u8"
        ariaLabel="Squat tutorial video"
      />,
    );

    expect(
      screen.getByRole('region', { name: 'Squat tutorial video' }),
    ).toBeInTheDocument();
  });

  it('renders the data-vjs-player container', () => {
    const { container } = render(
      <VideoPlayer src="https://example.com/video.m3u8" />,
    );

    expect(container.querySelector('[data-vjs-player]')).toBeInTheDocument();
  });
});
