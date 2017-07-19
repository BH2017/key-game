package com.brave.test;

/**
 * 关于代码注释问题---为了更好的展现程序结构逻辑，我注释尽可能不占行，而使其跟随代码行
 */
import javax.swing.JOptionPane;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
/**
 * @author zzf
 *20170711
 * */
public class KeyGame extends Application {
	private final String score_text_fixed = "*得分：";
	private final String wrong_text_fixed = "*出错：";
	private final String character_tips_text = "*提示：start<-a->e";//提示信息最长:确定控件x
	private final String tips_text_fixed = "*提示：";
	private final String pause_text_array[] = {"<<暂停>>","<<继续>>"};
	private final String back_text_menu = "<<返回菜单>>";
	private final int normal_text_font_size = 20;
	private final int tips_font_size = 15;
	private final double window_width = 800;
	private final double window_height = 600;
	private final double scene_height = window_height-30;//30是标题栏高度
	private final double produce_character_start_y = normal_text_font_size+5;
	private final double produce_character_end_y = 
			scene_height-produce_character_start_y;
	private final double down_text_y = scene_height-5;
	private final double tips_text_x=(
			window_width-character_tips_text.length()*tips_font_size)/2;
	private final Color color_default_program = Color.BLACK;
	private final int character_step_length = 20;
	private final double up_character_y = produce_character_start_y+normal_text_font_size;
	private Scene scene = null;
	private Group group = null;
	private Image images_cursor[] = null;
	private Image image_icon = null;
	private ImageCursor cursors[] = null;
	private Text text_score = null;
	private Text text_wrong = null;
	private Text text_tips = null;
	private Text text_character_current = null;
	private Text text_pause = null;
	private Text text_back_menu = null;
	private Text text_fly_around[] = null;//4个方向
	private Integer score = 0;
	private Integer error_num = 0;
	private char current_character = '#';
	private Character key_character_share_threadSelfDefine_keyPressEvent = '=';
	private Thread thread_character_move = null;
	private void initGroup(){
		group = new Group();
		initTextScore();
		initTextWrong();
		initTextTips();
		initTextCharacterCurrent();
		initTextPause();
		initTextBackMenu();
		initRunData();//减少耦合，赋值顺序问题
		initTextFlyAround();
		paintHorizontalLineOnScene();//------------test--------------
	}
	private void initTextFlyAround(){//避免频繁申请空间
		text_fly_around = new Text[4];
		for(int i=0;i<4;i++){
			text_fly_around[i] = new Text();
			text_fly_around[i].setFont(new Font(normal_text_font_size));
		}
		group.getChildren().addAll(text_fly_around);
	}
	private void setFlyCharacterStart(char ch1,double x,double y){
		for(Text text:text_fly_around){
			text.setText(ch1+"");
			text.setTranslateX(x);
			text.setTranslateY(y);
			text.setStroke(Color.rgb((int)(Math.random()*255), 
					(int)(Math.random()*255), (int)(Math.random()*255)));
			text.setFill(Color.rgb((int)(Math.random()*255), 
					(int)(Math.random()*255), (int)(Math.random()*255)));
		}
	}
	private void initRunData(){//初始化运行数据---严格顺序----
		text_character_current.setText(""+(current_character=getOneRandomCharacter()));
		text_tips.setText(getNewTips());
		setScoreBoth(0);
		setErrorNumBoth(0);
		thread_character_move = new Thread(){//---------启动线程-------
			public void run() {threadForChracterMove();}};
		thread_character_move.start();
	}
	private void dealGameOver(){//widget.scale自身缩放
		thread_character_move.interrupt();
		scene.setOnKeyPressed(keyEvent->{/*使忽略键盘响应*/});
		Text text_game_over = new Text("Game Over");
		text_game_over.setFont(new Font(100));
		group.getChildren().add(text_game_over);
		text_game_over.setTranslateX((window_width-
				text_game_over.getText().length()*60)/2);
		text_game_over.setTranslateY(0);
		text_game_over.setStroke(Color.ORANGERED);
		text_game_over.setFill(Color.ORANGERED);
		Timeline timeline = new Timeline();
		timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(2), 
				new EventHandler<ActionEvent>(){
			public void handle(ActionEvent arg0) {
				JOptionPane.showMessageDialog(null, "继续游戏or返回菜单");
				System.exit(2);
			}},new KeyValue(text_game_over.translateYProperty(), 300)
		));
		timeline.play();
	}
	private void threadForChracterMove(){
		while(true){
			if(thread_character_move.isInterrupted()){
				return;
			}
			try{Thread.sleep(1000);}catch (InterruptedException e) {System.out.println("thread is waked up");}
			/*在等待短暂时间后，再进行判断是否到底部：：避免直接跳到底部结束了:有个线程同步问题，，需要对字符改变的把握--由于使用的是同一个Text对象：只是里面的值改变，也是为了不频繁申请对象*/
			synchronized (key_character_share_threadSelfDefine_keyPressEvent) {
				if(text_character_current.getTranslateY() >=
						produce_character_end_y-normal_text_font_size){
					setErrorNumBoth(error_num+=1);
					if(error_num >= 10)
						dealGameOver();
					nextCharacterInit();
				}
			}
			text_character_current.setTranslateY(
					text_character_current.getTranslateY()+character_step_length);
		}
	}
	private void setErrorNumBoth(int error_num){
		this.error_num = error_num;
		text_wrong.setText(wrong_text_fixed+error_num);
	}
	private void setScoreBoth(int score){
		this.score = score;
		text_score.setText(score_text_fixed+score);
	}
	private String getNewTips(){
		String str = tips_text_fixed;
		if(current_character > 'a' && current_character<'z' 
				||current_character > 'A' && current_character < 'Z'
				||current_character > '0' && current_character < '9'){
			str = str+(char)(current_character-1)+"<-"+current_character
					+"->"+(char)(current_character+1);
		}else if(current_character == 'a' || current_character == 'A'
				||current_character =='0'){
			str = str+"start_<-"+current_character+"->"+(char)(current_character+1);
		}else if(current_character == 'z' || current_character == 'Z'
				||current_character == '9'){
			str=str+(char)(current_character-1)+"<-"+current_character+"->end_";
		}else
			System.out.println("没有可用的生成字符");
		return str;
	}
	private boolean isValidCharacter(char ch){
		if(ch>='A'&&ch<='Z'||ch>='a'&&ch<='z'||'0'<=ch&&'9'>=ch)
			return true;
		return false;
	}
	private char getOneRandomCharacter(){//a-zA-Z0-9
		int type_character = (int)(Math.random()*3);
		int position_character = (int)(Math.random()*26);
		char ch='+';
		switch (type_character) {
		case 0:ch=(char)('a'+position_character);break;
		case 1:ch=(char)('A'+position_character);break;
		case 2:ch=(char)('0'+position_character%10);break;
		default:break;
		}
		return ch;
	}
	private void initTextBackMenu(){
		text_back_menu = new Text(back_text_menu);
		group.getChildren().add(text_back_menu);
		text_back_menu.setFont(new Font(normal_text_font_size));
		text_back_menu.setTranslateX(window_width-
				normal_text_font_size*back_text_menu.length());
		text_back_menu.setTranslateY(down_text_y);
		text_back_menu.setFill(color_default_program);
	}
	private void initTextPause(){
		text_pause = new Text(pause_text_array[0]);
		group.getChildren().add(text_pause);
		text_pause.setFont(new Font(normal_text_font_size));
		text_pause.setTranslateX(0);
		text_pause.setTranslateY(down_text_y);
		text_pause.setFill(color_default_program);
	}
	private void initTextCharacterCurrent(){
		text_character_current = new Text(current_character+"");
		group.getChildren().add(text_character_current);
		text_character_current.setFont(new Font(normal_text_font_size));
		text_character_current.setTranslateX(200);
		text_character_current.setTranslateY(200);
	}
	private void paintHorizontalLineOnScene(){
		Line line_up = new Line(0, produce_character_start_y, 
				window_width, produce_character_start_y);
		group.getChildren().add(line_up);
		line_up.setStroke(Color.LIGHTCORAL);
		Line line_down = new Line(0, produce_character_end_y,
				window_width, produce_character_end_y);
		group.getChildren().add(line_down);
		line_down.setStroke(Color.LIGHTCYAN);
	}
	private void initTextTips(){
		text_tips = new Text(character_tips_text);
		group.getChildren().add(text_tips);
		text_tips.setFont(new Font(tips_font_size));
		text_tips.setTranslateY(produce_character_start_y-3);
		text_tips.setTranslateX(tips_text_x);
		text_tips.setFill(color_default_program);
	}
	private void initTextWrong(){
		text_wrong = new Text(wrong_text_fixed);
		group.getChildren().add(text_wrong);
		text_wrong.setFont(new Font(normal_text_font_size));
		int text_length = normal_text_font_size*(wrong_text_fixed.length()+1);
		text_wrong.setTranslateX(window_width-text_length);
		text_wrong.setTranslateY(normal_text_font_size);
		text_wrong.setFill(color_default_program);
	}
	private void initTextScore(){
		text_score = new Text(score_text_fixed);
		group.getChildren().add(text_score);
		text_score.setFont(new Font(normal_text_font_size));
		text_score.setTranslateX(0);
		text_score.setTranslateY(20);
		text_score.setFill(color_default_program);
	}
	private void loadImageAndAboutIt(){
		cursors = new ImageCursor[2];
		try{
			image_icon = new Image("keygame.png");
		}catch(Exception e){
			System.out.println("没有找到图标图片");
		}
		images_cursor = new Image[2];
		try{
			images_cursor[0] = new Image("cursor_normal.png");
			cursors[0] = new ImageCursor(images_cursor[0]);
		}catch (Exception e) {
			// TODO: handle exception
			System.out.println("没有找到光标一一般时候图片");
		}
		try{
			images_cursor[1] = new Image("cursor_press.png");
			cursors[1] = new ImageCursor(images_cursor[1]);
		}catch(Exception e){
			System.out.println("没有找到光标按下去的图片");
		}
	}
	private void initScene(){
		initGroup();
		scene = new Scene(group);
		scene.setFill(Color.LIGHTGREEN);
		if(cursors[0] != null)
			scene.setCursor(cursors[0]);
		if(cursors[0]!=null&&cursors[1] != null){
			scene.setOnMousePressed(me->{
				scene.setCursor(cursors[1]);
			});
			scene.setOnMouseReleased(me->{
				scene.setCursor(cursors[0]);
			});
		}
		scene.setOnKeyPressed(keyEvent->{
			String str = keyEvent.getText();
			char c1 = '-';
			synchronized (key_character_share_threadSelfDefine_keyPressEvent) {
				if(str.length()!=0 && isValidCharacter(c1=str.charAt(0))){
						if(c1 == current_character){//key---right
							setScoreBoth(score+=10);
							nextCharacterInit();
						}else{//key-----error
							setErrorNumBoth(error_num+=1);
							if(error_num == 10)
								dealGameOver();
						}
				}
			}
		});
	}
	private void nextCharacterInit(){
		setFlyCharacterStart(current_character, 
				text_character_current.getTranslateX(),
				text_character_current.getTranslateY());
		Timeline timeline = new Timeline();
		timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1),
				(ActionEvent)->{/*不做任何事*/},
				new KeyValue(text_fly_around[0].translateYProperty(),0),
				new KeyValue(text_fly_around[1].translateYProperty(),
						scene_height+normal_text_font_size),
				new KeyValue(text_fly_around[2].translateXProperty(),
						0-normal_text_font_size),
				new KeyValue(text_fly_around[3].translateXProperty(), 
						window_width+normal_text_font_size)
				));
		timeline.play();
		text_character_current.setText(""+
				(current_character=
				key_character_share_threadSelfDefine_keyPressEvent
						=getOneRandomCharacter()));
		text_character_current.setTranslateY(up_character_y);
		text_tips.setText(getNewTips());
	}
	private void initStage(Stage primaryStage){
		initScene();
		primaryStage.setScene(scene);
		primaryStage.setTitle("_打_*_字_*_游_*_戏_");
		primaryStage.setWidth(window_width);
		primaryStage.setHeight(window_height);
		primaryStage.setResizable(false);
		if(image_icon != null)
			primaryStage.getIcons().add(image_icon);
		primaryStage.setOnCloseRequest(windowEvent->{
			System.out.println("程序点X关闭");
			System.exit(1);
		});
	}
	@Override
	public void start(Stage arg0) throws Exception {
		// TODO Auto-generated method stub
		loadImageAndAboutIt();
		initStage(arg0);
		arg0.show();
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}
}