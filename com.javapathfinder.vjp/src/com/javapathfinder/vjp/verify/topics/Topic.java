/*******************************************************************************
 * Copyright © 2015 Saad Naji. All Rights Reserved.
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
package com.javapathfinder.vjp.verify.topics;

/**
 *
 * @author sandro
 * @author Saad Naji
 */
public class Topic {

    private String content;

    public Topic(){
        this("");
    }

    public Topic(String content){
        setContent(content);
    }

    public String toString(){
        return content;
    }

    public void setContent(String content){
        this.content = content;
    }

    public String getContent(){
        return content;
    }

}
