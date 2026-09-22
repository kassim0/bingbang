import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GamesSearchPopupComponent } from './games-search-popup.component';

describe('ListGamesPopupComponent', () => {
  let component: GamesSearchPopupComponent;
  let fixture: ComponentFixture<GamesSearchPopupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GamesSearchPopupComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GamesSearchPopupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
