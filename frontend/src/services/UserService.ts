import axios from "axios";
import { User } from "../model/User";


export class UserService {
    registerUser(user: User): Promise<any> {
        let body = {
            name: user.username,
            email: user.email,
            password: user.password
        };
        return fetch("api/user", { method: "POST", body: JSON.stringify(body) }).then(
            res => res.text()
        );
    };
    userLogin(user: User): Promise<any> {
        let body = {
            user: user.username,
            pass: user.password
        };
        return fetch("api/user/login", {
            method: "POST",
            body: JSON.stringify(body)
        }).then(res => res.text());
    };

    userLogout(): Promise<any> {
        return axios.get("/api/user/logout");
    };

}

export const userService = new UserService();