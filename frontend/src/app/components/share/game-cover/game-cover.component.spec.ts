import {ComponentFixture, TestBed} from '@angular/core/testing';
import {GameCoverComponent} from './game-cover.component';

describe('GameCoverComponent', () => {
  let fixture: ComponentFixture<GameCoverComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({imports: [GameCoverComponent]}).compileComponents();
    fixture = TestBed.createComponent(GameCoverComponent);
  });

  function render(url: string | null): HTMLElement {
    fixture.componentInstance.url = url;
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('shows a decorative, lazy-loaded image', () => {
    const image = render('https://media.rawg.io/media/games/a.jpg').querySelector('img');

    expect(image?.getAttribute('src')).toBe('https://media.rawg.io/media/games/a.jpg');
    expect(image?.getAttribute('alt')).toBe('');
    expect(image?.getAttribute('loading')).toBe('lazy');
  });

  it('asks IGDB for the small version of the cover', () => {
    const image = render('https://images.igdb.com/igdb/image/upload/t_cover_big/co1rs4.jpg').querySelector('img');

    expect(image?.getAttribute('src')).toBe('https://images.igdb.com/igdb/image/upload/t_cover_small/co1rs4.jpg');
  });

  it('shows a placeholder instead of a broken image when the game has no cover', () => {
    const image = render(null).querySelector('img');

    expect(image?.getAttribute('src')).toMatch(/^data:image\/svg\+xml/);
  });

  it('falls back to the placeholder when the image cannot be loaded', () => {
    const image = render('https://images.igdb.com/igdb/image/upload/t_cover_big/missing.jpg').querySelector('img')!;

    image.dispatchEvent(new Event('error'));
    fixture.detectChanges();

    expect(image.getAttribute('src')).toMatch(/^data:image\/svg\+xml/);
  });
});
