package com.lixinyang.zhoukao_zhou2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bwie.xlistviewlibrary.View.XListView;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements XListView.IXListViewListener {
    List<Bean.DataBean> list = new ArrayList<Bean.DataBean>();
    private ListView lv;
    int aa;
    int bb;
    String uri;
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

           adapter.notifyDataSetChanged();
            uiComplete();
        }
    };
    private XListView xlv;
    private MyAdapter adapter;
    private ImageLoader instance;
    private DrawerLayout dl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = (ListView) findViewById(R.id.lv);
        xlv = (XListView) findViewById(R.id.xlv);
        dl = (DrawerLayout) findViewById(R.id.dl);
        xlv.setPullLoadEnable(true);  //让XListView有上拉加载的功能;
        xlv.setXListViewListener(MainActivity.this);// 接口回调要把接口实现类设置进去, MainActivity就是(XListViewListener)它的实现了
        instance = ImageLoader.getInstance();

        indata();
        onclick();
        adapter = new MyAdapter();
        xlv.setAdapter(adapter);
    }

    private void onclick() {
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                list.clear();
                aa=1;
                uri="http://api.expoon.com/AppNews/getNewsList/type/"+(i+1)+"/p/"+aa;
                bb=i+1;
                setUri(aa);
                httpclick();
                dl.closeDrawer(lv);
                //Toast.makeText(MainActivity.this,""+i,Toast.LENGTH_LONG).show();
            }
        });
    }

    private void indata() {
        List<String> list = new ArrayList<String>();
        list.add("新闻");
        list.add("关注");
        list.add("动态");
        list.add("设置");
        ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1, list);
        lv.setAdapter(stringArrayAdapter);
    }
    public void setUri(int cc){
        uri="http://api.expoon.com/AppNews/getNewsList/type/"+bb+"/p/"+cc;

    }
    public void httpclick(){
        new Thread() {
            @Override
            public void run() {
                super.run();
                DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(uri);
                try {
                    SSLSocketFactory.getSocketFactory().setHostnameVerifier(new AllowAllHostnameVerifier());
                    //得到服务器返回的数据;
                    HttpResponse response = defaultHttpClient.execute(httpPost);
                    //得到状态码
                    int statusCode = response.getStatusLine().getStatusCode();
                    if(statusCode ==200){
                        //entiry 里面封装的数据;
                        HttpEntity entity = response.getEntity();
                        //这个result就是json字符串，剩下的就是解析工作了；
                        String result = EntityUtils.toString(entity);
                        Gson gson=new Gson();
                        Bean bean = gson.fromJson(result, Bean.class);
                        List<Bean.DataBean> data = bean.getData();
                        list.addAll(data);
                        Message msg = new Message();
                        handler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void onRefresh() {
        list.clear();
        aa=1;
        setUri(aa);
        httpclick();
    }

    @Override
    public void onLoadMore() {
        aa++;
        setUri(aa);
        httpclick();
    }
    class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public int getItemViewType(int position) {
            String pic_url = list.get(position).getPic_url();
            if(pic_url!=null){
                return 0;
            }else{
                return 1;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            int viewType = getItemViewType(i);
                switch(viewType){
                        case 0:
                        Holder1 holder1=null;
                        if(view==null){
                            holder1=new Holder1();
                            view=View.inflate(MainActivity.this,R.layout.activity_list1,null);
                            holder1.textView1=view.findViewById(R.id.tv1);
                            holder1.imageView1=view.findViewById(R.id.iv1);
                            view.setTag(holder1);
                        }else{
                            holder1  = (Holder1) view.getTag();
                        }
                        holder1.textView1.setText(list.get(i).getNews_title());
                        instance.displayImage(list.get(i).getPic_url(),holder1.imageView1);
                        break;
                        case 1:
                            Holder2 holder2=null;
                            if(view==null){
                                holder2=new Holder2();
                                view=View.inflate(MainActivity.this,R.layout.activity_list2,null);
                                holder2.textView2=view.findViewById(R.id.tv2);
                                view.setTag(holder2);
                            }else{
                                 holder2 = (Holder2) view.getTag();
                            }
                            holder2.textView2.setText(list.get(i).getNews_title());
                        break;
                    }
            return view;
        }
    }
    class Holder1{
        TextView textView1;
        ImageView imageView1;
    }
    class Holder2{
        TextView textView2;
    }
    private  void uiComplete(){
        xlv.stopRefresh();//停止刷新
        xlv.stopLoadMore();//停止上拉加载更多
        Date date = new Date();
        //Calendar instance = Calendar.getInstance();
        xlv.setRefreshTime(date.getHours()+";"+date.getMinutes()+";"+date.getSeconds());
    }
}
