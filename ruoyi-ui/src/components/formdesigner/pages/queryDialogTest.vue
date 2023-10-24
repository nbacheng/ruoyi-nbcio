<!--elementUI 图标选择器-->
<template>
  <div>
    <query-dialog
      v-model="value"
      :title="title"
      :showIndex="true"
      :multi="true"
      :columnConf="columnConf"
      :action="action"
    ></query-dialog>
    显示当前选择的值:{{ value }}
    <el-input v-model="title"></el-input>
    字段配置<el-input v-model="columnConf" type="textarea"></el-input>
  </div>
</template>

<script>
import queryDialog from "../components/extend/fancyDialogList";
export default {
  components: {
    queryDialog,
  },
  data() {
    return {
      value: "",
      title: "",
      columnConf:
        '[{"label":"姓名","property":"name","width":"250","show":true,"search":true},{"label":"日期","property":"date","width":"150","show":true,"search":true},{"label":"地址","property":"address","width":"","show":true,"search":true}]',
      action:
        "https://www.fastmock.site/mock/51715c0157535b99010bde55f2df33c8/formDesigner/api/dialogListData",
    };
  },
  methods: {},
  watch: {},
};
</script>
