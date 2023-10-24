<template>
  <div class="text">
    <svg class="icon" aria-hidden="true" v-if="!isElIcon">
      <use :xlink:href="iconFont"></use>
    </svg>
    <i :class="code" v-if="isElIcon"></i>
    {{text}}
  </div>
</template>
<script>
  export default {
    name:'icon',
    props:['code','text'],
    computed: {
      isElIcon(){ //判断是否为element icon
        return this.code.startsWith('el-');
      },
      iconFont(){
        return '#icon-'+this.code;
      }
    }
  }
</script>
<style scoped>
.icon{
  width:20px;
  height:20px;
  vertical-align:-5px;
}
</style>