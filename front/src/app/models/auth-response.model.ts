import { RolModel } from './rol.model';

export interface AuthResponseModel {
  token: string;
  role: RolModel;
}
