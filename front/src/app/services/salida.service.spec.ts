import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { SalidaService } from './salida.service';

describe('SalidaService', () => {
  let service: SalidaService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(SalidaService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
