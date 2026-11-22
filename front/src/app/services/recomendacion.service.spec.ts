import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { RecomendacionService } from './recomendacion.service';

describe('RecomendacionService', () => {
  let service: RecomendacionService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(RecomendacionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
