package fi.vm.sade.kayttooikeus.model;

import java.io.Serializable;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

public class PostgresIntegerArrayType implements UserType {
  @Override
  public int[] sqlTypes() {
      return new int[]{Types.ARRAY};
  }

  @Override
  public Class returnedClass() {
      return Integer[].class;
  }

  @Override
  public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
    throws HibernateException, SQLException {
      Array array = rs.getArray(names[0]);
      return array != null ? array.getArray() : null;
  }

  @Override
  public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session)
    throws HibernateException, SQLException {
      if (value != null && st != null) {
          Array array = session.connection().createArrayOf("int", (Integer[])value);
          st.setArray(index, array);
      } else if (st != null) {
          st.setNull(index, sqlTypes()[0]);
      }
  }

  @Override
  public boolean equals(Object x, Object y) throws HibernateException {
    return Arrays.equals((Integer[]) x, (Integer[]) y);
  }

  @Override
  public int hashCode(Object x) throws HibernateException {
    return Arrays.hashCode((Integer[]) x);
  }

  @Override
  public Object deepCopy(Object value) throws HibernateException {
    return ((Integer[]) value).clone();
  }

  @Override
  public boolean isMutable() {
    return false;
  }

  @Override
  public Serializable disassemble(Object value) throws HibernateException {
    return null;
  }

  @Override
  public Object assemble(Serializable cached, Object owner) throws HibernateException {
    return null;
  }

  @Override
  public Object replace(Object original, Object target, Object owner) throws HibernateException {
    return original;
  }
}
