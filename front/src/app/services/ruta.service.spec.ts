import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { RutaService } from './ruta.service';

describe('RutaService', () => {
  let service: RutaService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(RutaService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
