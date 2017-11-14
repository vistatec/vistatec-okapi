/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
-----------------------------------------------------------------------------
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
===========================================================================*/

package net.sf.okapi.steps.inconsistencycheck;

class Duplicate {

    private String text;
    private String display;
    private String docId;
    private String subDocId;
    private String tuId;
    private String segId;

    public Duplicate(String docId, String subDocId, String tuId, String segId, String text, String display) {
        this.display = display;
        this.text = text;
        this.docId = docId;
        this.subDocId = subDocId;
        this.tuId = tuId;
        this.segId = segId;
    }

    public String getText() {
        return text;
    }

    public String getDisplay() {
        return display;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getSubDocId() {
        return subDocId;
    }

    public void setSubDocId(String subDocId) {
        this.subDocId = subDocId;
    }

    public String getTuId() {
        return tuId;
    }

    public void setTuId(String tuId) {
        this.tuId = tuId;
    }

    public String getSegId() {
        return segId;
    }

    public void setSegId(String segId) {
        this.segId = segId;
    }
}
