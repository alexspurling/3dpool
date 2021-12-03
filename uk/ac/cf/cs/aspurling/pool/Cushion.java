package uk.ac.cf.cs.aspurling.pool;
import static org.lwjgl.opengl.GL11.*;
import uk.ac.cf.cs.aspurling.pool.util.Vector3D;

public class Cushion implements Collidable {

	private Vector3D point1;
	private Vector3D point2;
	private Vector3D plane;
	private Vector3D normal;	//points towards the play area
	
	private float cushionWidth;
	private float cushionHeight;
	private float leftAngle;
	private float rightAngle;
	private float cushionLength;

	private Wall walls[] = new Wall[3];	//each cushion has 3 colliding walls
	
	//Use static display lists for each cushion
	protected static int dlCushionEnd = 0;
	protected static int dlCushionSide = 0;
	
	
	//The play area must always lie to the left of the 
	//vector point2 - point1
	public Cushion(Vector3D point1, Vector3D point2, float leftAngle, float rightAngle) {
		this.point1 = point1;
		this.point2 = point2;
		plane = point2.subtract(point1);
		normal = (new Vector3D(0, 1, 0)).cross(plane);
		normal.normalize();
		
		cushionWidth = Table.CUSHION_WIDTH;
		cushionHeight = Table.CUSHION_HEIGHT;
		
		cushionLength = (float)(point1.subtract(point2).length());
		
		this.leftAngle = leftAngle;
		this.rightAngle = rightAngle;
		
		Vector3D corner1 = point1.add(plane.unit().multiply(rightAngle).add(normal.multiply(cushionWidth)));
		Vector3D corner2 = point2.add(plane.unit().multiply(-leftAngle).add(normal.multiply(cushionWidth)));

		walls[0] = new Wall(point1, corner1);
		walls[1] = new Wall(corner1, corner2);
		walls[2] = new Wall(corner2, point2);
		
		
		//Create display lists if not already created
		if (point1.z == point2.z) {
			if (dlCushionEnd == 0) {
				dlCushionEnd = glGenLists(1);
				glNewList(dlCushionEnd, GL_COMPILE);
				createCushion();
				glEndList();
			}
		}else if(point1.x == point2.x) {
			if (dlCushionSide == 0) {
				dlCushionSide = glGenLists(1);
				glNewList(dlCushionSide, GL_COMPILE);
				createCushion();
				glEndList();
			}
		}
	}

	private void createCushion() {
		
		final float CONTACT_HEIGHT = 0.01f;
		final float BASE_WIDTH = 0.02f;
		
		float baseAngleLeft = BASE_WIDTH * leftAngle / cushionWidth;
		float baseAngleRight = BASE_WIDTH * rightAngle / cushionWidth;
		float halfLength = cushionLength * 0.5f;
		
		glBegin(GL_QUADS);
		{
			//Draw top of cushion
			glNormal3f(0,1,0);
			glVertex3f(halfLength,cushionHeight,0);
			glVertex3f(-halfLength,cushionHeight,0);
			glVertex3f(-halfLength+leftAngle,cushionHeight,cushionWidth);
			glVertex3f(halfLength-rightAngle,cushionHeight,cushionWidth);
			
			//Draw contact area
			glNormal3f(0,0,1);
			glVertex3f(halfLength-rightAngle,cushionHeight,cushionWidth);
			glVertex3f(-halfLength+leftAngle,cushionHeight,cushionWidth);
			glVertex3f(-halfLength+leftAngle,cushionHeight-CONTACT_HEIGHT,cushionWidth);
			glVertex3f(halfLength-rightAngle,cushionHeight-CONTACT_HEIGHT,cushionWidth);
			
			//Draw bottom part
			Vector3D normal = new Vector3D(0,-(cushionWidth-BASE_WIDTH),cushionHeight);
			normal.normalize();
			glNormal3f((float)normal.x,(float)normal.y,(float)normal.z);
			glVertex3f(halfLength-rightAngle,cushionHeight-CONTACT_HEIGHT,cushionWidth);
			glVertex3f(-halfLength+leftAngle,cushionHeight-CONTACT_HEIGHT,cushionWidth);
			glVertex3f(-halfLength+baseAngleLeft,0,BASE_WIDTH);
			glVertex3f(halfLength-baseAngleRight,0,BASE_WIDTH);
		}
		glEnd();
		
		glBegin(GL_TRIANGLE_STRIP);
		{
			//Draw left part
			Vector3D normal = new Vector3D(-BASE_WIDTH,0,baseAngleLeft);
			normal.normalize();
			glNormal3f((float)normal.x,(float)normal.y,(float)normal.z);
			glVertex3f(-halfLength+leftAngle,cushionHeight,cushionWidth);
			glVertex3f(-halfLength,cushionHeight,0);
			glVertex3f(-halfLength+leftAngle,cushionHeight-CONTACT_HEIGHT,cushionWidth);
			glVertex3f(-halfLength,0,0);
			glVertex3f(-halfLength+baseAngleLeft,0,BASE_WIDTH);
			
		}
		glEnd();

		glBegin(GL_TRIANGLE_STRIP);
		{
			//Draw right part
			Vector3D normal = new Vector3D(BASE_WIDTH,0,baseAngleRight);
			normal.normalize();
			glNormal3f((float)normal.x,(float)normal.y,(float)normal.z);
			glVertex3f(halfLength-rightAngle,cushionHeight,cushionWidth);
			glVertex3f(halfLength,cushionHeight,0);
			glVertex3f(halfLength-rightAngle,cushionHeight-CONTACT_HEIGHT,cushionWidth);
			glVertex3f(halfLength,0,0);
			glVertex3f(halfLength-baseAngleRight,0,BASE_WIDTH);
			
		}
		glEnd();
	}
	
	public void render() {
		glPushMatrix();
		{
			if (point1.z == point2.z) {
				//Render bottom or top cushion
				glTranslatef(0,0,(float)point1.z);
				if (point1.x < point2.x) {
					//Render top cushion
					glRotatef(180,0,1,0);
				}
				glCallList(dlCushionEnd);
			}else if(point1.x == point2.x) {
				//Render a side cushion
				glTranslatef((float)point1.x, 0,(float)(point1.z + point2.z) * 0.5f);
				if (point1.z < point2.z) {
					//Render a left cushion
					glRotatef(90,0,1,0);
					if (point1.z > 0) {
						//Draw bottom left cushion
						glScalef(-1,1,1);
					}
				}else{
					//Render a right cushion
					glRotatef(-90,0,1,0);
					if (point1.z < 0) {
						//Draw top right cushion
						glScalef(-1,1,1);
					}
				}
				glCallList(dlCushionSide);
			}
		}
		glPopMatrix();
	}
	
	public Collision getCollision(Ball ball, float dt) {
		Collision earliestCol = null;
		Collision curCol;
		for (int i = 0; i < walls.length; i++) {
			if (walls[i] != null) {
				curCol = walls[i].getCollision(ball, dt);
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
		}
		return earliestCol;
	}

	public void handleCollision(Collision collision, float dt) {
		//walls[0].handleCollision(collision, dt);
	}
	
	protected static void deleteDisplayLists() {
		glDeleteLists(dlCushionEnd, 1);
		glDeleteLists(dlCushionSide, 1);
		dlCushionEnd = 0;
		dlCushionSide = 0;
	}

}
