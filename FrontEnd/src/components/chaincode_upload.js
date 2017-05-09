/**
 * oxchain ivoice app
 *
 *
 * Author: Jun
 * Email: iyakexi@gmail.com
 * Date: 05/05/2017
 *
 */

import React, { Component } from 'react';
import { Field, reduxForm } from 'redux-form';
import { connect } from 'react-redux';
import { uploadChainCode } from '../actions/chaincode'

class ChainCodeUpload extends Component {
  constructor(props) {
    super(props);
    this.state = { error:null, spin:false };
  }

  handleFormSubmit({ name, version, lang, chaincode }) {
      console.log({ name, version, lang, chaincode })
      //console.log(this.refs.chaincode.value)
      //const file = this.refs.chaincode.value
      if(name && version && lang && chaincode) {
        this.setState({ spin:true });
        this.props.uploadChainCode({ name, version, lang, chaincode }, err => {
          this.setState({ error: err ? err : null, spin:false });
          console.log('callback:'+err)
          this.props.addCallback(err);
        });
    }
  }

  renderAlert() {
    if (this.state.error) {
      return (
        <div className="alert alert-danger alert-dismissable">
          {this.state.error}
        </div>
      );
    }
  }

  renderField({ input, label, type, meta: { touched, error } }) {
    return (
      <div className={`form-group has-feedback ${touched && error ? 'has-error' : ''}`}>
        <label>{label}</label>
        <input {...input} placeholder={label} type={type} className="form-control"/>
        <div className="help-block with-errors">{touched && error ? error : ''}</div>
      </div>
    )}

  renderSelect({ label, meta: { touched, error } }) {
    return (
      <div className={`form-group has-feedback ${touched && error ? 'has-error' : ''}`}>
        <label>{label}</label>
        <Field name="lang" component="select" className="form-control">
          <option></option>
          <option value="go">go</option>
          <option value="java">java</option>
        </Field>
        <div className="help-block with-errors">{touched && error ? error : ''}</div>
      </div>
    )
  }

  render() {
    const { handleSubmit} = this.props;
    return (
      <div>
        <div className="">
          <div className="login-box-body">
            <p className="login-box-msg" style={{fontSize: 24+'px'}}></p>
            {this.renderAlert()}
            <form className="form-signin" onSubmit={handleSubmit(this.handleFormSubmit.bind(this))}>
              <Field name="name" component={this.renderField} type="text"  label="名称" />
              <Field name="version" component={this.renderField} type="text"  label="版本号" />
              <Field name="lang" component={this.renderSelect} label="语言"/>
              <Field name="chaincode" component={this.renderField} type="file" label="合约" />
              <div className="row">
                <div className="col-xs-8">
                </div>
                <div className="col-xs-4">
                  <button type="submit" className="btn btn-primary pull-right"><i className={`fa fa-spinner fa-spin ${this.state.spin?'':'hidden'}`}></i> 提交 </button>
                </div>
              </div>

            </form>
          </div>
        </div>
      </div>
    );
  }
}


const validate = values => {
  const errors = {};

  if(!values.name) {
    errors.name = '名称不能为空';
  }

  if(!values.version) {
    errors.version = '版本不能为空';
  }

  if(!values.lang) {
    errors.lang = '请选择语言';
  }

  if(!values.chaincode) {
    errors.chaincode = '合约不能为空';
  }

  return errors
};


const reduxChainCodeUploadForm = reduxForm({
  form: 'UploadChaincodeForm',
  validate
})(ChainCodeUpload);

export default connect(null, { uploadChainCode })(reduxChainCodeUploadForm);