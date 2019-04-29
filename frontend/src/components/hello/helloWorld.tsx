import * as React from 'react';

interface Props{
    userName:string;
    createUser: () => any;
    login:()=>any;
}

export const HelloWorldComponent = (props : Props) => {
  return (
    <div>
      <h2 onClick={() => props.createUser()}>Hello Mr. {props.userName} !</h2>
      <h2 onClick={()=>props.login()}>login</h2>      
    </div>
  );
}