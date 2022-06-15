package com.github.durex.main;

import com.github.durex.service.ServiceB;
import com.github.durex.service.api.ServiceD;
import com.github.durex.service.api.ServiceE;
import com.github.durex.service.api.impl.ServiceDImpl;
import com.github.durex.service.api.impl.ServiceEImpl;
import com.github.durex.service.api.impl.ServiceFImpl;
import com.github.durex.service.api.impl.ServiceGImpl;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

@Dependent
public class BeanDef {
  @Produces
  public ServiceB serviceB() {
    return new ServiceB();
  }

  @Produces
  public ServiceD serviceD() {
    return new ServiceDImpl();
  }

  @Produces
  public ServiceE serviceE() {
    return new ServiceEImpl();
  }

  @Produces
  public ServiceFImpl serviceF() {
    return new ServiceFImpl();
  }

  /** Must return the specific implementation! Otherwise decorator won't find it. */
  @Produces
  public ServiceGImpl serviceG() {
    return new ServiceGImpl();
  }
}
