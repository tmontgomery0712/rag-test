import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StreaksComponent } from './streaks.component';

describe('StreaksComponent', () => {
  let component: StreaksComponent;
  let fixture: ComponentFixture<StreaksComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StreaksComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(StreaksComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
