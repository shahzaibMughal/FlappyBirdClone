package com.flappybird.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

public class FlappyBird extends ApplicationAdapter {
    SpriteBatch batch;
    Texture backgroundImg;

    //Related to Game Logic
    int gameState; // 0 means gameNotStarted , 1 is game is started, 2 means gameOver
    int temp;
    //ShapeRenderer shapeRenderer;//sirf bird,pipes vgaira k uper solid rectangle k liye istamal kiya tha ab iski zrort nahi
    Circle birdCircle;
    Rectangle topPipeRectangle,bottomPipeRectangle;
    Rectangle centerRectangle; // use to check pipes passed successfully or collision occur
    boolean stopCheckAgain;


    //Related to Bird
    Texture[] bird;
    int flapState;
    float midXcoordinateOfBird, midYcoordinateOfBird;
    int birdWidth, birdHeight;
    int velocity, fly; // for bird falling and flyting on touch


    //Related to pipes
    Texture[] topPipe;
    Texture[] bottomPipe;
    final int  NUMBER_OF_PIPES_SET = 2; //Note: 1 top + 1 bottom pipe is 1 set
    float[] ycoordinateOfTopPipe;
    float[] ycoordinateOfBottomPipe;
    int  Y_DISTANCE_BETWEEN_PIPES;
    boolean stopDrawingAgain = false; // prevents pipes from drawing again and again
    int shiftPipes; // use to shift pipes up Or down using random value
    int midYcoordinateOfScreen; // use to align pipes
    int[] xcoordinateOfPipesSet;
    int pipeWidth;
    int pipeHeight;
    int maxPipesShift, minPipeShift;


    //Related to gameManagement , matlb score & gameOver vgaira
    int score;
    BitmapFont bitmapFont,userScoreBitmapStr;
    Texture gameOver;




    @Override
    public void create() {
        batch = new SpriteBatch();
        backgroundImg = new Texture("bg.png");


        //Related to GameLogic
        gameState = 0;
        temp = 0;
//        shapeRenderer = new ShapeRenderer();
        birdCircle = new Circle();
        topPipeRectangle = new Rectangle();
        bottomPipeRectangle = new Rectangle();
        centerRectangle = new Rectangle();
        stopCheckAgain = true;



        //Related to Bird
        bird = new Texture[2];
        bird[0] = new Texture("bird.png");
        bird[1] = new Texture("bird2.png");
        flapState = 0;
        birdWidth = Gdx.graphics.getWidth() / 10;
        birdHeight = Gdx.graphics.getHeight() / 15;
        midXcoordinateOfBird = Gdx.graphics.getWidth() / 2 - birdWidth / 2;
        midYcoordinateOfBird = Gdx.graphics.getHeight() / 2 - birdHeight / 2;
        fly = 0;
        velocity = 10;


        //Related to Pipes
        topPipe = new Texture[NUMBER_OF_PIPES_SET];
        bottomPipe = new Texture[NUMBER_OF_PIPES_SET];
        for(int i=0; i<NUMBER_OF_PIPES_SET; i++)
        {
            topPipe[i] = new Texture("toppipe.png");
            bottomPipe[i] = new Texture("bottompipe.png");
        }
        midYcoordinateOfScreen = Gdx.graphics.getHeight()/2;
        ycoordinateOfTopPipe = new float[NUMBER_OF_PIPES_SET];
        ycoordinateOfBottomPipe = new float[NUMBER_OF_PIPES_SET];
        Y_DISTANCE_BETWEEN_PIPES = Gdx.graphics.getHeight()/8;
        pipeWidth = Gdx.graphics.getWidth()/6;
        pipeHeight = Gdx.graphics.getHeight();
        xcoordinateOfPipesSet = new int[NUMBER_OF_PIPES_SET];
        maxPipesShift = pipeHeight/4;
        minPipeShift = -(pipeHeight/4);



        //Related to gameManagement , matlb score & gameOver vgaira
        score = 0;
        bitmapFont = new BitmapFont();
        bitmapFont.getData().scale(10);
        bitmapFont.setColor(Color.WHITE);
        userScoreBitmapStr = new BitmapFont();
        userScoreBitmapStr.getData().scale(6);
        userScoreBitmapStr.setColor(Color.WHITE);
        gameOver = new Texture("game_over.png");

    }










    @Override
    public void render() {
        /******* Sub sy pehly background image ko set krna hy*/
        batch.begin();
        //shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        //shapeRenderer.setColor(Color.RED);// shapeRenderer use to detect collision between bird & pipes
        batch.draw(backgroundImg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());



        /******* Ab bird ko center main flying krty hovy display krvana hy
         *   aur set krna hy k jb touch ho to game start ho jaey aur bird girna shuru kr dy aur
         *   jb touch ho to bird fly kry
         *   aur jb bird screen k upper border yan bottom border ko touch kry to GameOver ho jaey
         */

        //* Displaying the flying bird in mid of the screen
        if (flapState == 0) {
            flapState = 1;
        } else {
            flapState = 0;
        }

        if (gameState == 0) // if game is not Started
        { // place the flying bird on the middle of the screen
            batch.draw(bird[flapState], midXcoordinateOfBird, midYcoordinateOfBird, birdWidth, birdHeight);
            if (Gdx.input.isTouched()) {// if user touch the screen then start the game
                gameState = 1;
            }
        }
        else if (gameState == 1) //gameState 1 means game is started
        {
            //*****Bird girna shuru kr dy aur Each touch pr bird fly kry


            // Explaination of following code:
            // frz krain y coordinate ki value 200 hy to each time 10(velocity) decrement ho ga Q k fly ki value 0 rehti hy
            //      matlb 200 sy 190 then 160 , 170, 160 and sooo on
            // ab jb b user touch kry to to fly ki value 50 ho jaey gi matlb (10-50) jo k -40 banta.
            //      frz krain k bird 160 y position pr hy So,
            //      midYcoordinate = 160 - (-40)
            //      midYcoordinte  = 160+40 = 200

            // aur 200 krny k bad fly ki value foran 0 ho jaey gi jis sy bird phr apni velocity (speed) sy
            // move krna shuru ho jaey ga.
            if (Gdx.input.isTouched()) {
                fly = 50;
            }
            batch.draw(bird[flapState], midXcoordinateOfBird, (midYcoordinateOfBird -= (velocity - fly)), birdWidth, birdHeight);
            fly = 0;
            // midxcoordinateOfbird give us the starting location of circle but we want mid of bird
            birdCircle.set(midXcoordinateOfBird+birdWidth/2,midYcoordinateOfBird+birdHeight/2,birdWidth/2);
//            shapeRenderer.circle(birdCircle.x,birdCircle.y,birdCircle.radius);
            centerRectangle.set(midXcoordinateOfBird-pipeWidth,0,5,Gdx.graphics.getHeight());
//            shapeRenderer.rect(centerRectangle.x,centerRectangle.y,centerRectangle.width,centerRectangle.height);





            /*****  ab pipes ko continously display krvana hy
             * hum 2 pipes ka ak set banaen gy one is topPipe second is bottomPipe,top aur bottom pipe k darmiyan
             space same rakhni hy aur pip k set ko randomly shift krty jana hy (you know what i mean)

             * hum 2 set banaen gy aur in set k darmiyan distance 3 pipes ki width k brabar hoga. aur hum inhi 2 set ki
             continously loop banaen gy. matlb jb pehla right sy totally left pr chaly jaey ga to usy 2sry set k 3pipes
             ki width k brabr fasly pr pechly lga dy hain. aur jb 2sra totally left side pr chala jaey ga to usy pehly
             set sy 3pipes falsy pr us k pechy lga dain gy.
             Inshort:
             Loop ka logic ye ho ga k billi apni hi ponch ko pakar rahi hy aur ponch pkri nahi ja rahi, So billi gooool
             gooool gum rahi hy.
             */

            //** Now first we Draw Pipes
            if(stopDrawingAgain == false)
            { // draw tubes on screen
                drawPipes();
                stopDrawingAgain = true; //this prevent the pipes from Drawing again & again. in this way we can draw only 2 setsOfPipes and loop them again & again.
            }
            else
            {
                movePipes_checkCollission();
            }


            bitmapFont.draw(batch, String.valueOf(score), 20, 150);

        }
        else if(gameState==2) //if GameState == 2 means if GameOver
        {

            userScoreBitmapStr.draw(batch,String.valueOf("Score: "+score),0,userScoreBitmapStr.getCapHeight()*2);
            //**** jb gameOver ho jaey to gameOver ka logo show to aur bird wapis apni jaga pr fly krna shuru krdy
            batch.draw(gameOver,(Gdx.graphics.getWidth()/4),Gdx.graphics.getHeight()/2+(birdHeight),Gdx.graphics.getWidth()/2,birdHeight);

            midYcoordinateOfBird = Gdx.graphics.getHeight() / 2 - birdHeight / 2; // because mid x is still
            batch.draw(bird[flapState],midXcoordinateOfBird,midYcoordinateOfBird,birdWidth,birdHeight);
            if (Gdx.input.isTouched())
            {// if user touch the screen then start the game
                drawPipes();
                gameState = 1;
                score=0;

            }
        }

        batch.end();
//        shapeRenderer.end();




    }


    public int generateRandomNumber(int min, int max) {
        // hmara function 0 > 20 tk hi number generate krn skta hy.. aur agr negitive number aaty to
        // result hamaisha negative number ho ga so is k liye koi hal nikalna hy k hmain -20 sy 20 tk number milain

        // solution
        if ((0 + (int) (Math.random() * 2)) == 0)   //result will be 0 or 1 because 2 is not included
        {
            min = 0;  // in this way we can get positive values from 0 to 20
        }
        //else do nothing
        return (min + (int) (Math.random() * max));
    }


    public void drawPipes()
    {

        // draw tubes on screen
        //setting the position of each pipe set
        for(int i=0; i<NUMBER_OF_PIPES_SET; i++)
        {
            // hmary pas 2 hi pipe set hain.
            // So first set ko right side pr screen k andr sy left ki tarf lana hy
            // aur second ko first set sy firstset ki width sy 3 times fasly pr display krvana hy
            if(i==0)
            {
                xcoordinateOfPipesSet[i] = Gdx.graphics.getWidth()+pipeWidth;
            }
            else
            {
                xcoordinateOfPipesSet[i] = xcoordinateOfPipesSet[i-1] + (pipeWidth*4);
            }
        }


        for(int i=0; i<NUMBER_OF_PIPES_SET; i++)
        {
            shiftPipes = generateRandomNumber(minPipeShift,maxPipesShift);
            ycoordinateOfTopPipe[i] =  (midYcoordinateOfScreen+Y_DISTANCE_BETWEEN_PIPES)+shiftPipes;
            ycoordinateOfBottomPipe[i] = (-midYcoordinateOfScreen-Y_DISTANCE_BETWEEN_PIPES)+shiftPipes; // pipe half nechy chala gya hy
            stopCheckAgain = false;

            batch.draw(topPipe[i],xcoordinateOfPipesSet[i],ycoordinateOfTopPipe[i],pipeWidth,pipeHeight);
            batch.draw(bottomPipe[i],xcoordinateOfPipesSet[i],ycoordinateOfBottomPipe[i],pipeWidth,pipeHeight);

            topPipeRectangle.set(xcoordinateOfPipesSet[i],ycoordinateOfTopPipe[i],pipeWidth,pipeHeight);
            bottomPipeRectangle.set(xcoordinateOfPipesSet[i],ycoordinateOfBottomPipe[i],pipeWidth,pipeHeight);
//                        shapeRenderer.rect(topPipeRectangle.x,topPipeRectangle.y,topPipeRectangle.width,topPipeRectangle.height);
//                        shapeRenderer.rect(bottomPipeRectangle.x,bottomPipeRectangle.y,bottomPipeRectangle.width,bottomPipeRectangle.height);


        }
        stopDrawingAgain = true;
    }


    public void movePipes_checkCollission()// move pipes from right to left continously and Check bird pass the pipe or collission occur
    {


        for (int i = 0; i < NUMBER_OF_PIPES_SET; i++) {
            batch.draw(topPipe[i], xcoordinateOfPipesSet[i] -= 4, ycoordinateOfTopPipe[i], pipeWidth, pipeHeight);
            batch.draw(bottomPipe[i], xcoordinateOfPipesSet[i] -= 4, ycoordinateOfBottomPipe[i], pipeWidth, pipeHeight);

            topPipeRectangle.set(xcoordinateOfPipesSet[i],ycoordinateOfTopPipe[i],pipeWidth,pipeHeight);
            bottomPipeRectangle.set(xcoordinateOfPipesSet[i],ycoordinateOfBottomPipe[i],pipeWidth,pipeHeight);
//                    shapeRenderer.rect(topPipeRectangle.x,topPipeRectangle.y,topPipeRectangle.width,topPipeRectangle.height);
//                    shapeRenderer.rect(bottomPipeRectangle.x,bottomPipeRectangle.y,bottomPipeRectangle.width,bottomPipeRectangle.height);
//                    Gdx.app.log("1234","pipe: "+topPipeRectangle.getX()+", bird: "+birdCircle.x);



            //***** Jb bird screen k upper border yan bottom border ko touch kry to gameState 2 krni hy
            //      because gameState 2 means Collission that means GameOver
            if (midYcoordinateOfBird < 4 || midYcoordinateOfBird > Gdx.graphics.getHeight() - bird[0].getHeight())
            { // agr bird bottom yan upper border ko touch kr ly to gameState 2 ho jaey (2 means gameOver)
                gameState = 2;
            }

            // Explaination of following code:
            //hum sirf ye chahty hain k following funtion bar bar run hony k bjaey sirf 1 dfa run ho.
            //Working of the following function in simple words:
            // following if block sirf ak dfa run ho ga aur dosri dfa sirf tb hi run ho ga jb
            // pipe jo k centerRectangle sy chota hy vo totally screen k left pr na chala jaey.
            if(stopCheckAgain!=true) // we only want to let the following function run only 1 time
            {
                if(centerRectangle.overlaps(topPipeRectangle))
                {
                    stopCheckAgain = true;
                    Gdx.app.log("12345","Pipe Passed, Scrore is: "+ (++score));
                }
            }




            if (xcoordinateOfPipesSet[i] < -pipeWidth) {
                if (i == 0) {
                    xcoordinateOfPipesSet[i] = Gdx.graphics.getWidth() + pipeWidth;
                    shiftPipes = generateRandomNumber(minPipeShift, maxPipesShift);
                    shiftPipes = minPipeShift;
                    ycoordinateOfTopPipe[i] = (midYcoordinateOfScreen+Y_DISTANCE_BETWEEN_PIPES)+shiftPipes;
                    ycoordinateOfBottomPipe[i] = (-midYcoordinateOfScreen-Y_DISTANCE_BETWEEN_PIPES)+shiftPipes;;
                    stopCheckAgain = false;

                } else // if i==1 or 2
                {
                    xcoordinateOfPipesSet[i] = xcoordinateOfPipesSet[i - 1] + (pipeWidth * 4);
                    shiftPipes = generateRandomNumber(minPipeShift, maxPipesShift);
                    ycoordinateOfTopPipe[i] = (midYcoordinateOfScreen+Y_DISTANCE_BETWEEN_PIPES)+shiftPipes;
                    ycoordinateOfBottomPipe[i] = (-midYcoordinateOfScreen-Y_DISTANCE_BETWEEN_PIPES)+shiftPipes;
                    stopCheckAgain = false;

                }
            }

            /******* Ab colission ko detect krna hy. agr bird kisi pipe sy tkraey to gameOver(gameState=2) kr dyni hy
             * is k liye hum bird k uper ak invisible circle banaen gy aur pipe k gird rectangle
             (see circle & rectangle coding above). Q k texture(images) sy collision detect krna mushkil hy
             */

            /********  Ab hum ny score calculate krna hy */
            // Now i have Draw the circle around the bird & rectangle around the pipes
            // and here we check collision.
            if(Intersector.overlaps(birdCircle,topPipeRectangle) || Intersector.overlaps(birdCircle,bottomPipeRectangle))
            {
                Gdx.app.log("1234","Collision Occurrrrrrrrrrrrrr.....");
                gameState=2;
                stopDrawingAgain = false; // pipes ko gameover hony pr jahan hain wahan hi draw krny k liye
            }

        }
    }





}
