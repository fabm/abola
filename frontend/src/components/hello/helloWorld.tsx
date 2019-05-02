import * as React from 'react';
import { UserRegister, UserLogin } from '../../reducers/user';

interface Props {
  userName: string;
  createUser: (userRegister: UserRegister) => any;
  loginUser: (login: UserLogin) => any;
}


export const HelloWorldComponent = (props: Props) => {
  var stateLogin: UserLogin = {
    user: null,
    pass: null
  }

  var stateUserRegister: UserRegister = {
    name: null,
    email: null,
    password: null
  }

  var hrls: React.FormEventHandler = (e) => {
    e.preventDefault();
    props.createUser(stateUserRegister);
  }

  var hls: React.FormEventHandler = (e) => {
    e.preventDefault();
    props.loginUser(stateLogin);
  }

  var cehrl: React.ChangeEventHandler<HTMLInputElement> = (e) => {
    stateUserRegister[e.target.id] = e.target.value
  }
  var ceh: React.ChangeEventHandler<HTMLInputElement> = (e) => {
    stateLogin[e.target.id] = e.target.value
  }

  return (
    <div>
      <div>
        <h1>register login</h1>
        <form onSubmit={hrls}>
          <label htmlFor="email">Email</label>
          <input id="email" type="text" onChange={cehrl}></input>
          <label htmlFor="login">Login</label>
          <input id="name" type="text" onChange={cehrl}></input>
          <label htmlFor="password">Password</label>
          <input id="password" type="password" onChange={cehrl}></input>
          <button>register</button>
        </form>
      </div>
      <div>
        <h1>login</h1>
        <form onSubmit={hls}>
          <label htmlFor="login">Login</label>
          <input id="user" type="text" onChange={ceh}></input>
          <label htmlFor="password">Password</label>
          <input id="pass" type="password" onChange={ceh}></input>
          <button>register</button>
        </form>
      </div>

      <h2>Hello Mr. {props.userName} !</h2>
    </div>
  );
}