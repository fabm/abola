export enum MAKERS {
  VOLKSVAGEN,
  RENAULT
}

export interface Car {
  make: MAKERS;
  model?: string;
  maturityDate?: string;
  price?: number;
}

export function carToJson(car:Car){
  return {...car, make:MAKERS[car.make]}
}