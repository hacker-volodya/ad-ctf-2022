import { TestBed } from '@angular/core/testing';

import { DropletService } from './droplet.service';

describe('DropletService', () => {
  let service: DropletService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DropletService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
