/**
 * Created by oxchain on 2017/7/14.
 */
/**
 * oxchain ivoice app
 *
 *
 * Author: Jun
 * Email: iyakexi@gmail.com
 * Date: 09/05/2017
 *
 */

import React, { Component } from 'react';
import { Field, reduxForm, formValueSelector } from 'redux-form';
import { connect } from 'react-redux';
import { UpdateChainCode } from '../actions/chaincode'
import ChainsSelector from './chains_selector';

class ChainCodeUpdate extends Component {
    constructor(props) {
        super(props);
        this.state = { error:null, spin:false };
    }

    componentWillMount() {
    }

    componentWillReceiveProps(props) {
        if(!props.selectedItem) {
            this.setState({error:null});
        }
    }

    handleFormSubmit({ args, endorsement, chain }) {
        if(!this.props.selectedItem) return;

        const { name, version } = this.props.selectedItem
        //console.log({ chain, name, version, args, endorsement });
        //console.log(this.props.selectedChain);

        if(chain && name && version && args && endorsement) {
            this.setState({ spin:true });
            this.props.UpdateChainCode({ chain, name, version, args, endorsement }, err => {
                this.setState({ error: err ? err : null, spin:false });
                this.props.actionCallback(err);
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
        )
    }


    renderTd( key ) {
        const { selectedItem } = this.props;
        if(!selectedItem) return <td></td>;
        return (<td>{selectedItem[key]}</td>)
    }


    render() {
        const { handleSubmit} = this.props;
        return (
            <div>
                <div className="">
                    <div className="">
                        {this.renderAlert()}
                        <table className="table table-bordered">
                            <tbody>
                            <tr><th>名称</th><th>版本号</th><th>语言</th></tr>
                            <tr>
                                {this.renderTd('name')}
                                {this.renderTd('version')}
                                {this.renderTd('lang')}
                            </tr>
                            </tbody>
                        </table>
                        <form className="form-signin" onSubmit={handleSubmit(this.handleFormSubmit.bind(this))}>
                            <Field name="args" component={this.renderField} type="text"  label="更新参数" />
                            <Field name="endorsement" component={this.renderField} type="file" label="配置文件(yaml)" />
                            <Field name="chain" component={ChainsSelector} className="form-control" label="选择链"/>

                            <div className="row">
                                <div className="col-xs-8">
                                    {this.state.spin?'更新耗时较长,请耐心等待...':''}
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

    if(!values.args) {
        errors.args = '更新参数不能为空';
    }

    if(!values.endorsement) {
        errors.endorsement = '配置文件不能为空';
    }

    if(!values.chain) {
        errors.chain = '请选择链';
    }
    return errors
};

const selector = formValueSelector('updateChainCodeForm') ;
const reduxChainCodeUpdateForm = reduxForm({
    form: 'updateChainCodeForm',
    validate
})(ChainCodeUpdate);

function mapStateToProps(state) {
    return {
        selectedChain:selector(state, 'chain')
    };
}


export default connect(mapStateToProps, { UpdateChainCode })(reduxChainCodeUpdateForm);