package uk.ac.cf.cs.aspurling.pool;
import static org.lwjgl.opengl.GL11.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.glu.Disk;

import uk.ac.cf.cs.aspurling.pool.util.GLColour;
import uk.ac.cf.cs.aspurling.pool.util.Texture;
import uk.ac.cf.cs.aspurling.pool.util.TextureLoader;
import uk.ac.cf.cs.aspurling.pool.util.Utilities;
import uk.ac.cf.cs.aspurling.pool.util.Vector3D;
import uk.ac.cf.cs.aspurling.pool.util.Defaults;

public class Table implements Collidable{

	//length of the table is along z axis
	//width of the table is along x axis

	//All dimensions are in metres
	private static final float INCH =  0.0254f; //1 inch in m
	private static final float PLAY_AREA_LENGTH = 1.85f; //72 * INCH; //6ft
	private static final float PLAY_AREA_WIDTH = 0.94f; //PLAY_AREA_LENGTH / 2; //3ft
	public static final float CUSHION_WIDTH = 0.04f; //1.8f * INCH; //1.8 inches
	public static final float CUSHION_HEIGHT = 1.3f * Ball.BALL_RADIUS; //Slightly higher than ball radius
	private static final float TABLE_EDGE = 0.13f; //4.5f * INCH; //4.5 inches
	public static final float SURFACE_LENGTH = PLAY_AREA_LENGTH + 2 * CUSHION_WIDTH;
	public static final float SURFACE_WIDTH = PLAY_AREA_WIDTH + 2 * CUSHION_WIDTH;
	public static final float TABLE_LENGTH = SURFACE_LENGTH + 2 * TABLE_EDGE;
	public static final float TABLE_WIDTH = SURFACE_WIDTH + 2 * TABLE_EDGE;
	public static final float D_RADIUS = SURFACE_WIDTH / 6f;
	private static final float BASE_LENGTH = TABLE_LENGTH * 0.95f;
	private static final float BASE_WIDTH = TABLE_WIDTH * 0.95f;
	private static final float BASE_HEIGHT = 0.4f;

	public static float POCKET_RADIUS; //2.25f * INCH; //4 inches
	private static final float CORNER_POCKET_ANGLE = 0.045f;
	private static final float SIDE_POCKET_ANGLE = 0.02f;

	float saWidth2 = SURFACE_WIDTH * 0.5f;
	float saLength2 = SURFACE_LENGTH * 0.5f;
	float cornerOffset = (float)((POCKET_RADIUS * 2) / Math.sqrt(2));
	float pocketOffset = cornerOffset/2;
	
	float endCushionLength;
	float sideCushionLength;
	
	private Vector3D pocketCoords[] = new Vector3D[12];

	public static float CUSHION_RESTITUTION;
	private Cushion[] cushions;
	private Pocket[] pockets;
	
	private int dlTableSurface;
	private int dlTableEdge;
	private Material cloth;
	private Material tableMaterial;
	private Texture clothTex;
	private Texture tableTex;

	public Table() {
		
		pocketCoords[0] = new Vector3D(-saWidth2+cornerOffset,0,-saLength2);
		pocketCoords[1] = new Vector3D(-saWidth2,0,-saLength2+cornerOffset);
		pocketCoords[2] = new Vector3D(-saWidth2,0,-POCKET_RADIUS);
		pocketCoords[3] = new Vector3D(-saWidth2,0,POCKET_RADIUS);
		pocketCoords[4] = new Vector3D(-saWidth2,0,saLength2-cornerOffset);
		pocketCoords[5] = new Vector3D(-saWidth2+cornerOffset,0,saLength2);
		pocketCoords[6] = new Vector3D(saWidth2-cornerOffset,0,saLength2);
		pocketCoords[7] = new Vector3D(saWidth2,0,saLength2-cornerOffset);
		pocketCoords[8] = new Vector3D(saWidth2,0,POCKET_RADIUS);
		pocketCoords[9] = new Vector3D(saWidth2,0,-POCKET_RADIUS);
		pocketCoords[10] = new Vector3D(saWidth2,0,-saLength2+cornerOffset);
		pocketCoords[11] = new Vector3D(saWidth2-cornerOffset,0,-saLength2);
		
		endCushionLength = (float)(pocketCoords[11].x - pocketCoords[0].x);
		sideCushionLength = (float)(pocketCoords[2].z - pocketCoords[1].z);
		
		//get the stored colour for the cloth
		String colour = Utilities.settings.getString("clothcolour", "green");
		if (colour.equals("green")) {
			cloth = new Material(new GLColour(0.0f,0.0f,0.0f),
					  new GLColour(0.0f,0.5f,0.0f),
					  new GLColour(0.0f,0.0f,0.0f),
					  10);
		}
		if (colour.equals("blue")) {
			cloth = new Material(new GLColour(0.0f,0.0f,0.0f),
					  new GLColour(0.0f,0.40f,0.56f),
					  new GLColour(0.0f,0.0f,0.0f),
					  10);
		}
		if (colour.equals("red")) {
			cloth = new Material(new GLColour(0.0f,0.0f,0.0f),
					  new GLColour(0.55f,0.15f,0.15f),
					  new GLColour(0.0f,0.0f,0.0f),
					  10);
		}
		
		tableMaterial = new Material(new GLColour(0.0f,0.0f,0.0f),
				  new GLColour(0.7f,0.7f,0.7f),
				  new GLColour(0.0f,0.0f,0.0f),
				  10);
		
	
		//Create the top, right, bottom and left cushions
		cushions = new Cushion[6];
		cushions[0] = new Cushion(pocketCoords[11], pocketCoords[0], CORNER_POCKET_ANGLE, CORNER_POCKET_ANGLE);
		cushions[1] = new Cushion(pocketCoords[1], pocketCoords[2], SIDE_POCKET_ANGLE, CORNER_POCKET_ANGLE);
		cushions[2] = new Cushion(pocketCoords[3], pocketCoords[4], CORNER_POCKET_ANGLE, SIDE_POCKET_ANGLE);
		cushions[3] = new Cushion(pocketCoords[5], pocketCoords[6], CORNER_POCKET_ANGLE, CORNER_POCKET_ANGLE);
		cushions[4] = new Cushion(pocketCoords[7], pocketCoords[8], SIDE_POCKET_ANGLE, CORNER_POCKET_ANGLE);
		cushions[5] = new Cushion(pocketCoords[9], pocketCoords[10], CORNER_POCKET_ANGLE, SIDE_POCKET_ANGLE);

		
		clothTex = TextureLoader.get().getTexture("images/cloth.png");
		tableTex = TextureLoader.get().getTexture("images/table2.png");

		pockets = new Pocket[6];
		pockets[0] = new Pocket(-saWidth2+pocketOffset,-saLength2+pocketOffset,POCKET_RADIUS);
		pockets[1] = new Pocket(-saWidth2,0,POCKET_RADIUS);
		pockets[2] = new Pocket(-saWidth2+pocketOffset,saLength2-pocketOffset,POCKET_RADIUS);
		pockets[3] = new Pocket(saWidth2-pocketOffset,saLength2-pocketOffset,POCKET_RADIUS);
		pockets[4] = new Pocket(saWidth2,0,POCKET_RADIUS);
		pockets[5] = new Pocket(saWidth2-pocketOffset,-saLength2+pocketOffset,POCKET_RADIUS);
		
		
		dlTableSurface = glGenLists(1);
		glNewList(dlTableSurface, GL_COMPILE);
		createTableSurface();
		glEndList();

		dlTableEdge = glGenLists(1);
		glNewList(dlTableEdge, GL_COMPILE);
		tableMaterial.render();
		createTableEdge();
		glEndList();

	}

	private void createTableSurface() {
		
		cloth.render();
		
		float saTop = SURFACE_LENGTH * 0.5f;
		float saBottom = -(SURFACE_LENGTH * 0.5f);
		float saRight = SURFACE_WIDTH * 0.5f;
		float saLeft = -(SURFACE_WIDTH * 0.5f);

		int numj = 16;
		
		String detail = Utilities.settings.getString("graphicsdetail", "high");
		if (detail.equals("low")) numj = 4;
		if (detail.equals("medium")) numj = 8;
		if (detail.equals("high")) numj = 16;
		if (detail.equals("very high")) numj = 32;
		
		int numi = numj / 2;
		
		float iStep = SURFACE_WIDTH / (float)numi;
		float jStep = SURFACE_LENGTH / (float)numj;
		

		
		
		
		//Draw the surface of the table
		for (int i = 0; i < numi; i++) {
			glBegin(GL_QUAD_STRIP);
			{
				glNormal3f(0,1,0);
				glTexCoord2f(i / (float)numi,1-0f);
				glVertex3f(saLeft + i * iStep,0,saTop);
				glTexCoord2f((i+1) / (float)numi,1-0f);
				glVertex3f(saLeft + (i+1) * iStep,0,saTop);
	
				for (int j = 1; j <= numj; j++) {
					glTexCoord2f(0f,0f);
					glTexCoord2f(i / (float)numi,1-(j / (float)numj));
					glVertex3f(saLeft + i * iStep,0,saTop - j * jStep);
					glTexCoord2f((i+1) / (float)numi,1-(j / (float)numj));
					glVertex3f(saLeft + (i+1) * iStep,0,saTop - j * jStep);
				}
			}
			glEnd();
			
		}
		glDisable(GL_TEXTURE_GEN_S);
		glDisable(GL_TEXTURE_GEN_T);

	}
	
	private void createTableEdge() {
		
		float PI = (float)Math.PI;
		
		//Create semi-cylinders
		glBegin(GL_QUAD_STRIP); {
			for (int i = 0; i <= 8; i++) {
				double thet = 5f*PI/4f - (Math.PI / 8) * i;
				glNormal3f(-(float)Math.cos(thet),0,(float)Math.sin(thet));
				glVertex3f((float)Math.cos(thet) * POCKET_RADIUS - saWidth2+pocketOffset,CUSHION_HEIGHT,(float)-Math.sin(thet) * POCKET_RADIUS - saLength2+pocketOffset);
				glVertex3f((float)Math.cos(thet) * POCKET_RADIUS - saWidth2+pocketOffset,0,(float)-Math.sin(thet) * POCKET_RADIUS - saLength2+pocketOffset);
			}
		}glEnd();

		glBegin(GL_QUAD_STRIP); {
			for (int i = 0; i <= 8; i++) {
				double thet = 3f*PI/2f - (Math.PI / 8) * i;
				glNormal3f(-(float)Math.cos(thet),0,(float)Math.sin(thet));
				glVertex3f((float)Math.cos(thet) * POCKET_RADIUS - saWidth2,CUSHION_HEIGHT,(float)-Math.sin(thet) * POCKET_RADIUS);
				glVertex3f((float)Math.cos(thet) * POCKET_RADIUS - saWidth2,0,(float)-Math.sin(thet) * POCKET_RADIUS);
			}
		}glEnd();

		glBegin(GL_QUAD_STRIP); {
			for (int i = 0; i <= 8; i++) {
				double thet = 7f*PI/4f - (Math.PI / 8) * i;
				glNormal3f(-(float)Math.cos(thet),0,(float)Math.sin(thet));
				glVertex3f((float)Math.cos(thet) * POCKET_RADIUS - saWidth2+pocketOffset,CUSHION_HEIGHT,(float)-Math.sin(thet) * POCKET_RADIUS + saLength2-pocketOffset);
				glVertex3f((float)Math.cos(thet) * POCKET_RADIUS - saWidth2+pocketOffset,0,(float)-Math.sin(thet) * POCKET_RADIUS + saLength2-pocketOffset);
			}
		}glEnd();

		glBegin(GL_QUAD_STRIP); {
			for (int i = 0; i <= 8; i++) {
				double thet = 9f*PI/4f - (Math.PI / 8) * i;
				glNormal3f(-(float)Math.cos(thet),0,(float)Math.sin(thet));
				glVertex3f((float)Math.cos(thet) * POCKET_RADIUS + saWidth2-pocketOffset,CUSHION_HEIGHT,(float)-Math.sin(thet) * POCKET_RADIUS + saLength2-pocketOffset);
				glVertex3f((float)Math.cos(thet) * POCKET_RADIUS + saWidth2-pocketOffset,0,(float)-Math.sin(thet) * POCKET_RADIUS + saLength2-pocketOffset);
			}
		}glEnd();

		glBegin(GL_QUAD_STRIP); {
			for (int i = 0; i <= 8; i++) {
				double thet = 5f*PI/2f - (Math.PI / 8) * i;
				glNormal3f(-(float)Math.cos(thet),0,(float)Math.sin(thet));
				glVertex3f((float)Math.cos(thet) * POCKET_RADIUS + saWidth2,CUSHION_HEIGHT,(float)-Math.sin(thet) * POCKET_RADIUS);
				glVertex3f((float)Math.cos(thet) * POCKET_RADIUS + saWidth2,0,(float)-Math.sin(thet) * POCKET_RADIUS);
			}
		}glEnd();

		glBegin(GL_QUAD_STRIP); {
			for (int i = 0; i <= 8; i++) {
				double thet = 11f*Math.PI/4f - (Math.PI / 8) * i;
				glNormal3f(-(float)Math.cos(thet),0,(float)Math.sin(thet));
				glVertex3f((float)Math.cos(thet) * POCKET_RADIUS + saWidth2-pocketOffset,CUSHION_HEIGHT,(float)-Math.sin(thet) * POCKET_RADIUS - saLength2+pocketOffset);
				glVertex3f((float)Math.cos(thet) * POCKET_RADIUS + saWidth2-pocketOffset,0,(float)-Math.sin(thet) * POCKET_RADIUS - saLength2+pocketOffset);
			}
		}glEnd();
		
		//Create Fan tops

		/*
		float sVector[] = {1.0f, 0, 0, 0};
		float tVector[] = {0, 0, 1.0f, 0};
		ByteBuffer temp = ByteBuffer.allocateDirect(16);
		
		glEnable(GL_TEXTURE_GEN_S);
		glEnable(GL_TEXTURE_GEN_T);
		glTexGenf(GL_S, GL_TEXTURE_GEN_MODE, GL_OBJECT_LINEAR);
		glTexGen(GL_S, GL_OBJECT_PLANE, (FloatBuffer)temp.asFloatBuffer().put(sVector).flip());
		
		glTexGenf(GL_T, GL_TEXTURE_GEN_MODE, GL_OBJECT_LINEAR);
		glTexGen(GL_T, GL_OBJECT_PLANE, (FloatBuffer)temp.asFloatBuffer().put(tVector).flip());
		*/
		
		glNormal3f(0,1,0);
		glBegin(GL_TRIANGLE_FAN); {
			vertTex(-TABLE_WIDTH/2f, CUSHION_HEIGHT, -TABLE_LENGTH/2f);
			vertTex(-TABLE_WIDTH/2f, CUSHION_HEIGHT, -saLength2+cornerOffset);
			for (int i = 0; i <= 8; i++) {
				double thet = 5f*PI/4f - (Math.PI / 8) * i;
				vertTex((float)Math.cos(thet) * POCKET_RADIUS - saWidth2+pocketOffset,CUSHION_HEIGHT,(float)-Math.sin(thet) * POCKET_RADIUS - saLength2+pocketOffset);
			}
			vertTex(-saWidth2+cornerOffset, CUSHION_HEIGHT, -TABLE_LENGTH/2f);
		}glEnd();
		
		glBegin(GL_TRIANGLE_FAN); {
			vertTex(-TABLE_WIDTH/2f, CUSHION_HEIGHT, 0);
			vertTex(-TABLE_WIDTH/2f, CUSHION_HEIGHT, POCKET_RADIUS);
			for (int i = 0; i <= 8; i++) {
				double thet = 3f*PI/2f - (Math.PI / 8) * i;
				vertTex((float)Math.cos(thet) * POCKET_RADIUS - saWidth2,CUSHION_HEIGHT,(float)-Math.sin(thet) * POCKET_RADIUS);
			}
			vertTex(-TABLE_WIDTH/2f, CUSHION_HEIGHT,-POCKET_RADIUS);
		}glEnd();
		
		glBegin(GL_TRIANGLE_FAN); {
			vertTex(-TABLE_WIDTH/2f, CUSHION_HEIGHT, TABLE_LENGTH/2f);
			vertTex(-saWidth2+cornerOffset, CUSHION_HEIGHT, TABLE_LENGTH/2f);
			for (int i = 0; i <= 8; i++) {
				double thet = 7f*PI/4f - (Math.PI / 8) * i;
				vertTex((float)Math.cos(thet) * POCKET_RADIUS - saWidth2+pocketOffset,CUSHION_HEIGHT,(float)-Math.sin(thet) * POCKET_RADIUS + saLength2-pocketOffset);
			}
			vertTex(-TABLE_WIDTH/2f, CUSHION_HEIGHT, saLength2-cornerOffset);
		}glEnd();
		
		glBegin(GL_TRIANGLE_FAN); {
			vertTex(TABLE_WIDTH/2f, CUSHION_HEIGHT, TABLE_LENGTH/2f);
			vertTex(TABLE_WIDTH/2f, CUSHION_HEIGHT, saLength2-cornerOffset);
			for (int i = 0; i <= 8; i++) {
				double thet = 9f*PI/4f - (Math.PI / 8) * i;
				vertTex((float)Math.cos(thet) * POCKET_RADIUS + saWidth2-pocketOffset,CUSHION_HEIGHT,(float)-Math.sin(thet) * POCKET_RADIUS + saLength2-pocketOffset);
			}
			vertTex(saWidth2-cornerOffset, CUSHION_HEIGHT, TABLE_LENGTH/2f);
		}glEnd();
		
		glBegin(GL_TRIANGLE_FAN); {
			vertTex(TABLE_WIDTH/2f, CUSHION_HEIGHT, 0);
			vertTex(TABLE_WIDTH/2f, CUSHION_HEIGHT, -POCKET_RADIUS);
			for (int i = 0; i <= 8; i++) {
				double thet = 5f*PI/2f - (Math.PI / 8) * i;
				vertTex((float)Math.cos(thet) * POCKET_RADIUS + saWidth2,CUSHION_HEIGHT,(float)-Math.sin(thet) * POCKET_RADIUS);
			}
			vertTex(TABLE_WIDTH/2f, CUSHION_HEIGHT,POCKET_RADIUS);
		}glEnd();

		glBegin(GL_TRIANGLE_FAN); {
			vertTex(TABLE_WIDTH/2f, CUSHION_HEIGHT, -TABLE_LENGTH/2f);
			vertTex(saWidth2-cornerOffset, CUSHION_HEIGHT, -TABLE_LENGTH/2f);
			for (int i = 0; i <= 8; i++) {
				double thet = 11f*PI/4f - (Math.PI / 8) * i;
				vertTex((float)Math.cos(thet) * POCKET_RADIUS + saWidth2-pocketOffset,CUSHION_HEIGHT,(float)-Math.sin(thet) * POCKET_RADIUS - saLength2+pocketOffset);
			}
			vertTex(TABLE_WIDTH/2f, CUSHION_HEIGHT, -saLength2+cornerOffset);
		}glEnd();

		//Create fill in quads
		glBegin(GL_QUADS); {
			vertTex(-TABLE_WIDTH/2f, CUSHION_HEIGHT, -saLength2+cornerOffset);
			vertTex(-TABLE_WIDTH/2f, CUSHION_HEIGHT, -POCKET_RADIUS);
			vertTex(-saWidth2, CUSHION_HEIGHT, -POCKET_RADIUS);
			vertTex(-saWidth2, CUSHION_HEIGHT, -saLength2+cornerOffset);

			vertTex(-TABLE_WIDTH/2f, CUSHION_HEIGHT, POCKET_RADIUS);
			vertTex(-TABLE_WIDTH/2f, CUSHION_HEIGHT, saLength2-cornerOffset);
			vertTex(-saWidth2, CUSHION_HEIGHT, saLength2-cornerOffset);
			vertTex(-saWidth2, CUSHION_HEIGHT, POCKET_RADIUS);
			
			vertTex(-saWidth2+cornerOffset, CUSHION_HEIGHT, saLength2);
			vertTex(-saWidth2+cornerOffset, CUSHION_HEIGHT, TABLE_LENGTH/2f);
			vertTex(saWidth2-cornerOffset, CUSHION_HEIGHT, TABLE_LENGTH/2f);
			vertTex(saWidth2-cornerOffset, CUSHION_HEIGHT, saLength2);

			vertTex(saWidth2, CUSHION_HEIGHT, POCKET_RADIUS);
			vertTex(saWidth2, CUSHION_HEIGHT, saLength2-cornerOffset);
			vertTex(TABLE_WIDTH/2f, CUSHION_HEIGHT, saLength2-cornerOffset);
			vertTex(TABLE_WIDTH/2f, CUSHION_HEIGHT, POCKET_RADIUS);

			vertTex(saWidth2, CUSHION_HEIGHT, -saLength2+cornerOffset);
			vertTex(saWidth2, CUSHION_HEIGHT, -POCKET_RADIUS);
			vertTex(TABLE_WIDTH/2f, CUSHION_HEIGHT, -POCKET_RADIUS);
			vertTex(TABLE_WIDTH/2f, CUSHION_HEIGHT, -saLength2+cornerOffset);

			vertTex(-saWidth2+cornerOffset, CUSHION_HEIGHT, -TABLE_LENGTH/2f);
			vertTex(-saWidth2+cornerOffset, CUSHION_HEIGHT, -saLength2);
			vertTex(saWidth2-cornerOffset, CUSHION_HEIGHT, -saLength2);
			vertTex(saWidth2-cornerOffset, CUSHION_HEIGHT, -TABLE_LENGTH/2f);
		}glEnd();
		
		//Create border
		glBegin(GL_QUAD_STRIP); {
			vertTex2(-TABLE_WIDTH/2f, CUSHION_HEIGHT, -TABLE_LENGTH/2f);
			vertTex2(-BASE_WIDTH/2f, -BASE_HEIGHT, -BASE_LENGTH/2f);
			vertTex2(-TABLE_WIDTH/2f, CUSHION_HEIGHT, TABLE_LENGTH/2f);
			vertTex2(-BASE_WIDTH/2f, -BASE_HEIGHT, BASE_LENGTH/2f);

			vertTex3(TABLE_WIDTH/2f, CUSHION_HEIGHT, TABLE_LENGTH/2f);
			vertTex3(BASE_WIDTH/2f, -BASE_HEIGHT, BASE_LENGTH/2f);

			vertTex2(TABLE_WIDTH/2f, CUSHION_HEIGHT, -TABLE_LENGTH/2f);
			vertTex2(BASE_WIDTH/2f, -BASE_HEIGHT, -BASE_LENGTH/2f);

			vertTex3(-TABLE_WIDTH/2f, CUSHION_HEIGHT, -TABLE_LENGTH/2f);
			vertTex3(-BASE_WIDTH/2f, -BASE_HEIGHT, -BASE_LENGTH/2f);
		}glEnd();
		
		//glDisable(GL_TEXTURE_GEN_S);
		//glDisable(GL_TEXTURE_GEN_T);
	}
	
	private void vertTex(float x, float y, float z) {
		//glTexCoord2f(x, TABLE_LENGTH/2f+z);
		glTexCoord2f(x*2, z*2);
		glVertex3f(x, y, z);
	}

	private void vertTex2(float x, float y, float z) {
		//glTexCoord2f(x, TABLE_LENGTH/2f+z);
		glTexCoord2f(y*2, z*2);
		glVertex3f(x, y, z);
	}
	
	private void vertTex3(float x, float y, float z) {
		//glTexCoord2f(x, TABLE_LENGTH/2f+z);
		glTexCoord2f(y*2, x*2);
		glVertex3f(x, y, z);
	}

	public void render() {
		
		glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_DECAL);
		
		glPushMatrix();
		{
			//Render Table
			clothTex.bind();
			//cloth.render();
			glCallList(dlTableSurface);
		}
		glPopMatrix();

		for (int i = 0; i < pockets.length; i++) {
			//cloth.render();
			pockets[i].render();
		}
		
		for (int i = 0; i < cushions.length; i++) {
			//cloth.render();
			glDisable(GL_CULL_FACE);
			cushions[i].render();
			glEnable(GL_CULL_FACE);
		}

		glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
		
		
		glPushMatrix();
		{
			//Render Table
			tableTex.bind();
			//cloth.render();
			glCallList(dlTableEdge);
		}
		glPopMatrix();
		
		

		
	}

	public Collision getCollision(Ball ball, float dt) {

		Collision earliestCol = null;
		Collision curCol;
		for (int i = 0; i < cushions.length; i++) {
			if (i==2) {
				i = 2;
			}
			curCol = cushions[i].getCollision(ball, dt);
			if (curCol != null) {
				if (earliestCol == null) {
					earliestCol = curCol;
				}else{
					if (curCol.time < earliestCol.time) {
						earliestCol = curCol;
					}
				}
			}
			curCol = pockets[i].getCollision(ball, dt);
			if (curCol != null) {
				if (earliestCol == null) {
					earliestCol = curCol;
				}else{
					if (curCol.time < earliestCol.time) {
						earliestCol = curCol;
					}
				}
			}
		}

		if (earliestCol != null) {
			if (earliestCol.time < 0) {
				int i = 0;
			}
		}
		return earliestCol;
	}
	public void handleCollision(Collision collision, float dt) {

	}

	public Vector3D getHeadSpot() {
		return new Vector3D(0,Ball.BALL_RADIUS,(SURFACE_LENGTH / 4));
	}

	public Vector3D getFootSpot() {
		return new Vector3D(0,Ball.BALL_RADIUS,-(SURFACE_LENGTH / 4));
	}
	
	public static void loadSettings(boolean newGame) {
		CUSHION_RESTITUTION = Utilities.settings.getFloat("tablecoeffrest",Defaults.CUSHION_RESTITUTION);
		POCKET_RADIUS = Utilities.settings.getFloat("pocketradius",Defaults.POCKET_RADIUS);
		//need to delete cushion and pocket display lists to reset pocket radius
		if (newGame) {
			Cushion.deleteDisplayLists();
			Pocket.deleteDisplayLists();
		}
	}
	
	public static void loadDefaults(boolean newGame) {
		CUSHION_RESTITUTION = Defaults.CUSHION_RESTITUTION;
		POCKET_RADIUS = Defaults.POCKET_RADIUS;
		if (newGame) {
			Cushion.deleteDisplayLists();
			Pocket.deleteDisplayLists();
		}
	}

}
