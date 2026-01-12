import { TestBed } from '@angular/core/testing';

import { StreakService } from './streak-service';
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('StreakService', () => {
  let service: StreakService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule
      ],
      providers: []
    });
    service = TestBed.inject(StreakService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
