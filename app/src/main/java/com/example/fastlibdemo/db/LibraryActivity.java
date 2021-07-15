package com.example.fastlibdemo.db;

import android.view.View;

import com.example.fastlibdemo.R;
import com.example.fastlibdemo.base.BindViewActivity;
import com.example.fastlibdemo.databinding.ActivityLibraryBinding;
import com.fastlib.annotation.Bind;
import com.fastlib.db.And;
import com.fastlib.db.Condition;
import com.fastlib.db.DataFromDatabase;
import com.fastlib.db.DatabaseNoDataResultCallback;
import com.fastlib.db.FastDatabase;
import com.fastlib.db.FunctionCommand;
import com.fastlib.utils.FastUtil;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库操作
 */
public class LibraryActivity extends BindViewActivity<ActivityLibraryBinding>{

    private  Gson gson = new Gson();
    private int scoree = 80;

    @Bind({R.id.btnSave,R.id.btnUpdate,R.id.btnDelete,R.id.btnQuery,R.id.btnDataFromDb})
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSave:
                List<ProvinceBeen> list = new ArrayList<>();

                ProvinceBeen bean = new ProvinceBeen();
                bean.name = "浙江省";
                bean.score = 85;
                List<ProvinceBeen.City> city = new ArrayList<>();
                city.add(new ProvinceBeen.City("杭州市"));
                city.add(new ProvinceBeen.City("诸暨市"));
                bean.cities =city;
                list.add(bean);

                FastDatabase.getDefaultInstance(this).saveOrUpdate(list);
                showText();
                break;
            case R.id.btnUpdate:
                ProvinceBeen data= new ProvinceBeen();
                data.id = 1;
                data.name = "福建省";
                data.score = scoree++;
                List<ProvinceBeen.City> cityUpdate = new ArrayList<>();
                cityUpdate.add(new ProvinceBeen.City("南平市"));
                data.cities =cityUpdate;

                FastDatabase.getDefaultInstance(this).saveOrUpdate(data);
                showText();
                break;
            case R.id.btnDelete:
                FastDatabase.getDefaultInstance(this)
                        .setFilter(And.condition(Condition.bigger("score",83)))
                        .delete(ProvinceBeen.class);
                showText();
                break;
            case R.id.btnQuery:
                showText();
                break;
            case R.id.btnDataFromDb:
                showTextFormDB();
                break;
        }
    }

    private void showText() {
        //同步获取数据库数据.
        StringBuilder sb = new StringBuilder();
        List<ProvinceBeen> list = FastDatabase.getDefaultInstance(this)
                .setFilter(And.condition(Condition.bigger("score",60)))
                .orderBy(true)//默认true
                .limit(0,Integer.MAX_VALUE)//默认Integer.MAX_VALUE
                .get(ProvinceBeen.class);
        if(list != null && !list.isEmpty()){
            for(ProvinceBeen p :list){
                sb.append(gson.toJson(p)).append("\n");
            }
        }
        //获取某列名下数据总值(不常用，一般为数值类型数据)
        ProvinceBeen been = FastDatabase.getDefaultInstance(this)
                .putFunctionCommand("score", FunctionCommand.sum())
                .getFirst(ProvinceBeen.class);
        if(been!=null){
            sb.append("分数总和："+been.score +" | 数据库版本:"+FastDatabase.getConfig().getVersion());
        }
        mViewBinding.tvContent.setText(sb.toString());
    }

    private void showTextFormDB() {
        FastDatabase.getDefaultInstance(LibraryActivity.this).delete(PersonBeen.class);

        PersonBeen rawData = new PersonBeen("明兰",28,"柔弱的外表下，有颗坚毅勇敢的心");

        FastDatabase.getDefaultInstance(this).saveOrUpdateAsync(FastUtil.listOf(rawData), new DatabaseNoDataResultCallback() {
            @Override
            public void onResult(boolean success) {
                if(success){
                    StringBuilder sb = new StringBuilder();
                    sb.append("非空构造时，从数据库获取数据 | 数据库版本:" + FastDatabase.getConfig().getVersion() + "\n");
                    List<PersonBeen> result = FastDatabase.getDefaultInstance(LibraryActivity.this)
                            .setConstructorParams(new Object[]{DataFromDatabase.from("name"), DataFromDatabase.from("age"),DataFromDatabase.from("intro")})
                            .get(PersonBeen.class);
                    if (result != null && !result.isEmpty()) {
                        for (PersonBeen p : result) {
                            sb.append(String.format("%s", gson.toJson(p)) + "\n");
                        }
                    }
                    mViewBinding.tvContent.setText(sb.toString());
                }
            }
        });
    }

    @Override
    public void alreadyPrepared() {

    }
}
