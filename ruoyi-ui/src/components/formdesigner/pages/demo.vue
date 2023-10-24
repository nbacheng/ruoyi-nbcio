<template>
    <el-card class="box-card" :body-style="{ padding: '0px' }">
    <div slot="header" style="padding:5px">
      <span>卡片名称</span>
      <el-button style="float: right; padding: 3px 0" type="text" @click="handlerHide">隐藏</el-button>
    </div>
    <div style="padding:20px;">
        <div v-for="o in 4" :key="o" class="text item" >
        {{'列表内容 ' + o }}
        </div> 
    </div>
  </el-card>
</template>
<script>
export default {
  data(){     
    return{
      iconDialogVisible:false,
    } 
  },
  methods:{
    handlerHide(){
      
    }
  }
}
</script>
  <style>
    .text {
      font-size: 14px;
    }
  
    .item {
      margin-bottom: 18px;
    }
  
    .clearfix:before,
    .clearfix:after {
      display: table;
      content: "";
    }
    .clearfix:after {
      clear: both
    }
  
    .box-card {
      width: 480px;
    }
  </style>
  