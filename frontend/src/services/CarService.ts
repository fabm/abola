import Axios from "axios";
import { Car, MAKERS } from "../model/Car";
export class CarService {
    createCar(car: Car): Promise<any> {
        return Axios
            .post("api/car", {...car,make:MAKERS[car.make]}, { withCredentials: true })
            .then(res => {
                console.log(res.data);
            });
    }

    getCar(car: Car): Promise<any> {
        return Axios.get("api/car")
    };

    fetchCars(): Promise<Car[]> {
        return Axios.get("api/car/list")
            .then(res => res.data as Array<any>)
            .then(res => {
                return res.map((item) => { 
                    item.make = MAKERS[item.make];
                    return item;
                })
            });
    };

}

export const carService = new CarService()