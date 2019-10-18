package tech.pegasys.artemis.networking.handel;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import tech.pegasys.artemis.datastructures.operations.Attestation;

public class HandelAttestationFactory {

  /**
   * Create attestations using traditional constructor for its components.
   *
   * @param t      type of Attestation to return
   * @param args   argument types for the constructor
   * @param params parameters for the constructor
   * @param <T>    The Attestation return type
   * @return
   */
  public static <T extends Attestation> T createAttestation(Class<T> t, Class[] args,
      Object[] params) {
    try {
      Constructor<T> constructor = t.getConstructor(args);
      Object obj = constructor.newInstance(params);
      if (obj instanceof Attestation) {
        return (T) obj;
      }
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Create attestations using builder for constructing each of its components.
   *
   * @param t      type of Attestation to return
   * @param args
   * @param params
   * @param <T>
   * @return
   */
  public static <T extends Attestation> T buildAttestation(Class<T> t, Class[] args,
      Builder[] params) {
    try {
      Constructor<T> constructor = t.getConstructor(args);
      return constructor.newInstance(Arrays.stream(params).map(b -> b.build()).toArray());
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    return null;
  }


  /**
   * Build a constructor for attestations that allow pre-defining the builders for some components.
   * Parameters passed as null form the parameters to the closure of the constructor. i.e.,
   * buildConstructorClosure( new Class[]{String.class, Integer.class}, new Builder{}
   *
   * @param t
   * @param args
   * @param params
   * @param <T>
   * @return
   */
  public static <T extends Attestation> BuiltConstructorClosure<T> buildConstructorClosure(
      Class<T> t,
      Class[] args, Builder[] params) {
    try {
      Constructor<T> constructor = t.getConstructor(args);
      return (buildargs, buildparams) -> {
        int j = 0;
        for (int i = 0; i < constructor.getParameterCount(); i++) {
          if (params[i] == null) {
            params[i] = buildparams[j++];
          }
        }
        return buildAttestation(t, args, params);
      };
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Simple interface for building things.
   *
   * @param <T>
   */
  interface Builder<T> {

    T build();
  }

  /**
   * Dynamic constructor interface for building things.
   *
   * @param <T>
   */
  interface BuiltConstructorClosure<T> {

    T build(Class[] args, Builder[] params);
  }
}
