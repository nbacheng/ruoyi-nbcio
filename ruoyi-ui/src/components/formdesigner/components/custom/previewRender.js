import { isAttr,jsonClone } from '../utils';
import childrenItem from './slot/index';
import {remoteData} from './mixin';
import { getToken } from "@/utils/auth";

//先修改在这里,后续需要优化
function vModel(self, dataObject) {
  dataObject.props.value = self.value;
  dataObject.on.input = val => {
    self.$emit('input', val)
  }
  //判断是否为上传组件
  if(self.conf.compType === 'upload'){
    //for token add by nbacheng 2022-09-07
    //dataObject.attrs['headers'] = {"Authorization":"Bearer " + getToken()};
    /**
     * 此处增加自定义的token,如果不能满足要求，可以重写此处代码
     */
    const token = getToken();
    dataObject.attrs['headers'] = {"Authorization":"Bearer " + token};
    console.log("dataObject.props.value",dataObject.props.value)
    if(dataObject.props.value!==undefined && dataObject.props.value !==''){
      const filevalue = JSON.parse(dataObject.props.value);
      dataObject.props['file-list'] = filevalue;
    }
    dataObject.attrs['before-upload'] = file=>{
      //非限定后缀不允许上传
      console.log("before-upload file",file);
      const fileName = file.name;
      console.log("before-upload fileName",fileName);
      const suffixName = fileName.split('.').pop();

      if(!self.conf.accept.includes(suffixName)){
        self.$message.error('该后缀文件不允许上传');
        return false;
      }
      const fileSize = file.size;
      if(fileSize>dataObject.props.fileSize*1024*1024){
        self.$message.error('文件大小超出限制，请检查！');
        return false;
      }
    }

    //for get return file url add by nbacheng 2022-09-07
    dataObject.attrs['on-success'] = file=>{

        console.log("on-success file",file);
        var filename,fileObj;
        if(file.data.hasOwnProperty('ossId') && file.data.ossId != "") {//oss上传
          filename=file.data.fileName.substring(file.data.fileName.lastIndexOf('/')+1)  //获取文件名称
          fileObj = {name: filename, url: file.data.url}
        }
        else {//本地上传
          filename=file.data.fileName.substring(file.data.fileName.lastIndexOf('/')+1)  //获取文件名称
          fileObj = {name: filename, url: process.env.VUE_APP_BASE_API + file.data.fileName}
        }

        console.log("dataObject=",dataObject);
        console.log("self.conf=",self.conf);
        let oldValue = [];
        if(dataObject.props.value) {
           oldValue = JSON.parse(dataObject.props.value);
        }else {
          oldValue = [];
        }
        if (oldValue) {
          oldValue.push(fileObj)
        } else {
          oldValue = [fileObj]
        }
        self.$emit('input',JSON.stringify(oldValue));
        console.log("on-success value",oldValue);
    }
    dataObject.attrs['on-remove'] = (file, fileList) => {
      console.log("on-remove file,fileList",file,fileList);
      let oldValue = JSON.parse(dataObject.props.value);
      console.log("on-remove oldValue",oldValue);
      //file 删除的文件
      //过滤掉删除的文件
      let newValue = oldValue.filter(item => item.name !== file.name)
      self.$emit('input',JSON.stringify(newValue));
      console.log("on-remove newValue",newValue);
    }

    dataObject.attrs['on-error'] = (file) => {
      console.log("on-error file",file);
    }

    dataObject.attrs['on-preview'] = (file) => {
      console.log("on-preview file",file);
      //download(file);
    }
    //for get return file url add by nbacheng 2022-09-07
  }
}

export default {
  render(h) {
    let dataObject = {
      attrs: {},
      props: {},
      on: {},
      style: {}
    }
    //远程获取数据
    this.getRemoteData();
    const confClone = jsonClone(this.conf);
    const children = childrenItem(h,confClone);

    Object.keys(confClone).forEach(key => {
      const val = confClone[key]
      if (dataObject[key]) {
        dataObject[key] = val
      } else if(key ==='width'){
        dataObject.style= 'width:'+val;
      } else if (!isAttr(key)) {
        dataObject.props[key] = val
      }else {
        if (key == 'classStyle' && val.length > 0){
          let style =""
          val.forEach(item =>{
            console.log(item)
            style+=item +" "
          })
          dataObject.attrs['class'] = style
        }else if (key == 'cssStyle'){
          dataObject.attrs['style'] = val
        }else if(key !== 'value'){
          dataObject.attrs[key] = val
        }
      }
    })
    /*调整赋值模式，规避cascader组件赋值props会出现覆盖预制参数的bug */
    vModel(this, dataObject);
    return h(confClone.ele, dataObject, children)
  },
  props: ['conf','value'],
  mixins:[remoteData]
}
