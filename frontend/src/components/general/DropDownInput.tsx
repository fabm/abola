import * as React from "react";

export interface DropDownInputProps {
  current:number
  element:{[index:number] : string};
  updateValue;
}

export const DropDownInput = (props: DropDownInputProps) =>
  <div>
    <h1>{props.element==null?"":props.element[props.current]}</h1>
  </div>;