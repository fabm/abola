import * as React from "react";
import classNames from "classnames/bind";

export interface DropDownInputProps {
  current: number
  element: { [index: number]: string };
  updateValue: (value: number) => any;
  isOpen;
  updateState: (isOpen:boolean) => any
}

export const DropDownInput = (props: DropDownInputProps) => {
  var classes = classNames(
    { "dropdown-menu": true },
    { show: props.isOpen }
  );

  let labelsCount: number = Object.keys(props.element).length / 2;
  let labels: string[] = [...Array(labelsCount).keys()].map(
    key => props.element[key]
  );


  return <div className="col-6">
    <div className="input-group mb-3">
      <div className="input-group-prepend">
        <button
          className="btn btn-outline-secondary dropdown-toggle"
          type="button"
          data-toggle="dropdown"
          aria-haspopup="true"
          aria-expanded="false"
          onClick={() => {props.updateState(!props.isOpen)}}
          onBlur={(event) => {props.updateState(false)}}
        >Choose maker...</button>
        <div className={classes}>
          {labels.map(label => {
            return (
              <a
                key={label}
                onMouseDown={(event) => event.preventDefault()}
                onClick={(event) => {
                  props.updateValue(props.element[label]);
                  props.updateState(false);
                }}
                className="dropdown-item"
                href="#"
              >
                {label}
              </a>
            );
          })}
        </div>
      </div>
      <input
        type="text"
        className="form-control"
        aria-label="Text input with dropdown button"
        value={props.current == null ? "" : props.element[props.current]}
        disabled
      />
    </div>
  </div>
}