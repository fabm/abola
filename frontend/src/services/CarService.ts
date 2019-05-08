import Axios from "axios";
import { Car } from "../model/Car";

export class CarService {
    createCar(car: Car): Promise<any> {
        return Axios
            .post("api/car", car, { withCredentials: true })
            .then(res => res.data);
    }

    getCar(car: Car): Promise<any> {
        return fetch("api/car", { method: "POST", body: JSON.stringify(car) }).then(
            res => res.json()
        );
    };

    fetchCars(): Promise<Car[]> {
        return fetch("api/car/list").then(res => res.json());
    };ß

}

export const carService = new CarService()