import * as React from 'react';

interface Props {
  userName: string;
}


export const HelloWorldComponent = (props: Props) => {


  return (
    <div>
      <h2>Hello Mr. {props.userName} !</h2>
    </div>
  );
}