import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BeaconsComponent } from './beacons.component';

describe('BeaconsComponent', () => {
  let component: BeaconsComponent;
  let fixture: ComponentFixture<BeaconsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BeaconsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BeaconsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
