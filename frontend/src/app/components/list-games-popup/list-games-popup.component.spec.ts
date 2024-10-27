import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListGamesPopupComponent } from './list-games-popup.component';

describe('ListGamesPopupComponent', () => {
  let component: ListGamesPopupComponent;
  let fixture: ComponentFixture<ListGamesPopupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListGamesPopupComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ListGamesPopupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
