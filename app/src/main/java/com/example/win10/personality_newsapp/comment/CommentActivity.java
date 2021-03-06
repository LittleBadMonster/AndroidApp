package com.example.win10.personality_newsapp.comment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.win10.personality_newsapp.PropertyUri;
import com.example.win10.personality_newsapp.R;
import com.example.win10.personality_newsapp.collection.CollectionActivity;
import com.example.win10.personality_newsapp.news_visit.NewsDetailActivity;
import com.example.win10.personality_newsapp.showcomment.TestAddCommentActivity;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommentActivity extends AppCompatActivity {
    private List<CommentBean> list;
    CommentListAdapter commentListAdapter;
    RequestQueue requestQueue ;
    private int page=1;
    private void putData(final HashMap<String, String> user_partinfo){
        try {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    new PropertyUri().host+"app/getComment/?user_id="+user_partinfo.get("user_id"), null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray data = response.getJSONArray("data");
                        Integer code= (Integer)response.get("code");
                        if (code==0){
                            for (int i=0;i<data.length();i++){
                                CommentBean commentitem=new CommentBean();
                                JSONObject item=data.getJSONObject(i);
                                commentitem.setNickname(user_partinfo.get("user_name"));
                                commentitem.setHeadpictureurl(user_partinfo.get("user_avatar_url"));
                                commentitem.setComment_content(item.getString("comment_text"));
                                commentitem.setRelease_time(item.getString("comment_time"));
                                commentitem.setNews_item("@"+item.getJSONArray("news_info").getJSONObject(0).getString("from")
                                        +":"+item.getJSONArray("news_info").getJSONObject(0).getString("title"));
                                commentitem.setNew_id(item.getJSONArray("news_info").getJSONObject(0).getLong("_id"));
                                commentitem.set_id(item.getInt("comment_id"));
                                list.add(commentitem);
                            }
                        }
                        if (list.size() == 0)
                        {
                            findViewById(R.id.refreshLayout_comment).setVisibility(View.GONE);
                            findViewById(R.id.refreshLayout_comment_empty).setVisibility(View.VISIBLE);
                        }else{
                            findViewById(R.id.refreshLayout_comment).setVisibility(View.VISIBLE);
                            findViewById(R.id.refreshLayout_comment_empty).setVisibility(View.GONE);
                        }
                        commentListAdapter.notifyDataSetChanged();
                    }catch (JSONException e){
                        Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(),"获取失败",Toast.LENGTH_LONG).show();
                }
            });
            requestQueue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        ListView listview = (ListView)findViewById(R.id.mylist);
        requestQueue= Volley.newRequestQueue(this);
        list= new ArrayList<CommentBean>();
        final HashMap<String, String> user_partinfo=(HashMap<String,String>)getIntent().getSerializableExtra("user_partinfo");
        putData(user_partinfo);
        commentListAdapter=new CommentListAdapter(this,requestQueue,list);
        listview.setAdapter(commentListAdapter);

        //listview.setEmptyView((TextView)findViewById(R.id.comentnovalue));

        // 删除全部收藏记录
        Button deleteall=(Button)findViewById(R.id.deleteall_comment);
        deleteall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder bb=new AlertDialog.Builder(CommentActivity.this);
                if(CommentActivity.this.list.size()<=0){
                    bb.setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    bb.setMessage("您还没参与评论哦");
                }else{
                    bb.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            HashMap<String, String> user_partinfo=(HashMap<String,String>)getIntent().getSerializableExtra("user_partinfo");
                            RequestQueue requestQueue= Volley.newRequestQueue(CommentActivity.this);
                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                                    new PropertyUri().host+"app/deleteAllComment?user_id="+user_partinfo.get("user_id"),
                                    null, new Response.Listener<JSONObject>() {
                                public void onResponse(JSONObject response) {
                                    Toast.makeText(getBaseContext(), "成功删除全部评论", Toast.LENGTH_SHORT).show();
                                    if (CommentActivity.this.list.size() == 0)
                                    {
                                        findViewById(R.id.refreshLayout_comment).setVisibility(View.GONE);
                                        findViewById(R.id.refreshLayout_comment_empty).setVisibility(View.VISIBLE);
                                    }else{
                                        findViewById(R.id.refreshLayout_comment).setVisibility(View.VISIBLE);
                                        findViewById(R.id.refreshLayout_comment_empty).setVisibility(View.GONE);
                                    }
                                }
                            }, new Response.ErrorListener() {
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(getBaseContext(), "出现网络问题", Toast.LENGTH_SHORT).show();
                                }
                            });
                            requestQueue.add(jsonObjectRequest);
                            CommentActivity.this.list.clear();
                            CommentActivity.this.commentListAdapter.notifyDataSetChanged();
                        }
                    });
                    bb.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    bb.setMessage("您确定要删除全部评论吗？");
                }
                bb.setTitle("提示");
                bb.show();
            }
        });

//        下拉刷新监听
        final RefreshLayout mRefreshLayout = findViewById(R.id.refreshLayout_comment);
        final RefreshLayout mRefreshLayoutempty = findViewById(R.id.refreshLayout_comment_empty);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                HashMap<String, String> user_partinfo=(HashMap<String,String>)getIntent().getSerializableExtra("user_partinfo");
                if(CommentActivity.this.list.size()>0){
                    CommentActivity.this.list.clear();
                    CommentActivity.this.commentListAdapter.notifyDataSetChanged();
                }
                putData(user_partinfo);
                CommentActivity.this.page=1;
                mRefreshLayout.finishRefresh();
            }
        });
        mRefreshLayoutempty.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                HashMap<String, String> user_partinfo=(HashMap<String,String>)getIntent().getSerializableExtra("user_partinfo");
                putData(user_partinfo);
                CommentActivity.this.page=1;
                mRefreshLayoutempty.finishRefresh();
            }
        });

//        上拉刷新获取更多监听
        mRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                try {
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                            new PropertyUri().host+"app/getComment/?user_id="+user_partinfo.get("user_id")+"&page="+CommentActivity.this.page, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray data = response.getJSONArray("data");
                                Integer code= (Integer)response.get("code");
                                if (code==0&&data.length()>0){
                                    for (int i=0;i<data.length();i++){
                                        CommentBean commentitem=new CommentBean();
                                        JSONObject item=data.getJSONObject(i);
                                        commentitem.setNickname(user_partinfo.get("user_name"));
                                        commentitem.setHeadpictureurl(user_partinfo.get("user_avatar_url"));
                                        commentitem.setComment_content(item.getString("comment_text"));
                                        commentitem.setRelease_time(item.getString("comment_time"));
                                        commentitem.setNews_item("@"+item.getJSONArray("news_info").getJSONObject(0).getString("from")
                                                +":"+item.getJSONArray("news_info").getJSONObject(0).getString("title"));
                                        commentitem.setNew_id(item.getJSONArray("news_info").getJSONObject(0).getLong("_id"));
                                        commentitem.set_id(item.getInt("comment_id"));
                                        list.add(commentitem);
                                    }
                                    commentListAdapter.notifyDataSetChanged();
                                    CommentActivity.this.page++;
                                    mRefreshLayout.finishLoadMore();
                                }else {
                                    mRefreshLayout.setNoMoreData(true);
                                    mRefreshLayout.finishLoadMore();
                                }

                            }catch (JSONException e){
                                Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
                            }
                        }
                    }, new Response.ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(),"获取失败",Toast.LENGTH_LONG).show();
                        }
                    });
                    requestQueue.add(jsonObjectRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


//       跳转监听
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("news_id",String.valueOf(list.get(position).getNew_id()));
                intent.setClass(CommentActivity.this, NewsDetailActivity.class);
                CommentActivity.this.startActivity(intent);
            }
        });
//        长按删除评论监听
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean  onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder bb=new AlertDialog.Builder(CommentActivity.this);
                bb.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HashMap<String, String> user_partinfo=(HashMap<String,String>)getIntent().getSerializableExtra("user_partinfo");
                        RequestQueue requestQueue= Volley.newRequestQueue(CommentActivity.this);
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                                new PropertyUri().host+"app/deleteComment?user_id="+user_partinfo.get("user_id")
                                        +"&comment_id="+CommentActivity.this.list.get(position).get_id(),
                                null, new Response.Listener<JSONObject>() {
                            public void onResponse(JSONObject response) {
                                Toast.makeText(getBaseContext(), "删除成功", Toast.LENGTH_SHORT).show();
                                if (CommentActivity.this.list.size() == 0)
                                {
                                    findViewById(R.id.refreshLayout_comment).setVisibility(View.GONE);
                                    findViewById(R.id.refreshLayout_comment_empty).setVisibility(View.VISIBLE);
                                }else{
                                    findViewById(R.id.refreshLayout_comment).setVisibility(View.VISIBLE);
                                    findViewById(R.id.refreshLayout_comment_empty).setVisibility(View.GONE);
                                }
                            }
                        }, new Response.ErrorListener() {
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getBaseContext(), "出现网络问题", Toast.LENGTH_SHORT).show();
                            }
                        });
                        requestQueue.add(jsonObjectRequest);
                        if(CommentActivity.this.list.remove(position)!=null){
                            System.out.println("success");
                        }else {
                            System.out.println("failed");
                        }
                        CommentActivity.this.commentListAdapter.notifyDataSetChanged();
                    }
                });
                bb.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                bb.setMessage("您确定要删除该条评论吗？");
                bb.setTitle("提示");
                bb.show();
                return true;
            }
        });

    }

}
