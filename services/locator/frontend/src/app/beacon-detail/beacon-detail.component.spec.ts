import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BeaconDetailComponent } from './beacon-detail.component';

describe('BeaconDetailComponent', () => {
  let component: BeaconDetailComponent;
  let fixture: ComponentFixture<BeaconDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BeaconDetailComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BeaconDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
