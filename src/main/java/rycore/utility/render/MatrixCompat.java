package rycore.utility.render;

import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;

/**
 * Lop tuong thich giua MatrixStack (3D) va Matrix3x2fStack (GUI 2D).
 *
 * <p>Tu 1.21.6 {@code DrawContext.getMatrices()} tra ve {@link Matrix3x2fStack} - mot
 * ma tran affine 2D - thay cho {@link MatrixStack} 4x4 truoc day. Toan bo tang HUD va
 * ClickGUI cua RyCore van tinh toan tren MatrixStack, nen can cau noi hai chieu.
 */
public final class MatrixCompat {
   private MatrixCompat() {
   }

   /**
    * Dung MatrixStack 3D tu transform 2D hien tai cua DrawContext, de code ve cu
    * (nhan Matrix4f) dung duoc nguyen ven.
    */
   public static MatrixStack toMatrixStack(Matrix3x2fStack stack) {
      MatrixStack result = new MatrixStack();
      result.multiplyPositionMatrix(to4f(stack));
      return result;
   }

   /** Nang ma tran affine 2D len 4x4 de dung voi cac ham ve nhan Matrix4f. */
   public static Matrix4f to4f(Matrix3x2f m) {
      // Matrix3x2f luu theo cot: (m00 m01) (m10 m11) (m20 m21)
      return new Matrix4f(
         m.m00, m.m01, 0.0F, 0.0F,
         m.m10, m.m11, 0.0F, 0.0F,
         0.0F, 0.0F, 1.0F, 0.0F,
         m.m20, m.m21, 0.0F, 1.0F
      );
   }

   /** Ma tran vi tri de truyen vao cac ham ve cua Render2DEngine. */
   public static Matrix4f positionMatrix(Matrix3x2fStack stack) {
      return to4f(stack);
   }
}
