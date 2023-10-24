<template>
    <div>
        <el-form-item label="Class样式">
            <el-select v-model="data.classStyle" placeholder="请选择" multiple>
                <el-option
                        v-for="item in classStyleOptions()"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value">
                </el-option>
            </el-select>
        </el-form-item>
        <el-form-item label="Css样式" >
            <el-input
                    type="textarea"
                    :rows="3"
                    placeholder="请输入内容"
                    v-model="data.cssStyle">
            </el-input>
        </el-form-item>
    </div>

</template>

<script>
    import {getClassCss} from '../../utils/index'

    const css = require('css');
    export default {
        name: "customStyle",
        props: {
            formConf: {
                type: Object,
                default: {}
            },
            data: {
                type: Object,
                default: {}
            },
        },
        methods: {
            classStyleOptions() {
                let data = []
                if (this.formConf.classStyle) {
                    try {
                        let ast = getClassCss(css.parse(this.formConf.classStyle))
                        Object.keys(ast).map(key => {
                            data.push({label: key, value: key.substring(1)})
                        })
                    } catch (e) {

                    }
                }
                return data
            }
        }

    }


</script>
