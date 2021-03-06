/* Copyright (c) 2014-2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.developerstudio.datamapper.diagram.edit.parts;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.ManhattanConnectionRouter;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionNodeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ITreeBranchEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewRequest;
import org.eclipse.gmf.runtime.draw2d.ui.figures.PolylineConnectionEx;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.swt.graphics.Color;
import org.wso2.developerstudio.datamapper.diagram.edit.parts.InNode2EditPart.InNode2Figure;
import org.wso2.developerstudio.datamapper.diagram.edit.parts.InNode3EditPart.InNode3Figure;
import org.wso2.developerstudio.datamapper.diagram.edit.parts.InNodeEditPart.InNodeFigure;
import org.wso2.developerstudio.datamapper.diagram.edit.parts.OutNode2EditPart.OutNode2Figure;
import org.wso2.developerstudio.datamapper.diagram.edit.parts.custom.CustomNonResizableEditPolicyEx;
import org.wso2.developerstudio.datamapper.diagram.edit.policies.DataMapperLinkItemSemanticEditPolicy;

/**
 * @generated
 */
public class DataMapperLinkEditPart extends ConnectionNodeEditPart implements ITreeBranchEditPart {

	/**
	 * @generated
	 */
	public static final int VISUAL_ID = 4001;

	/**
	 * @generated NOT
	 */
	protected int alpha = 255;

	/**
	 * @generated
	 */
	public DataMapperLinkEditPart(View view) {
		super(view);
	}

	/**
	 * @generated NOT
	 */
	@Override
	public boolean canAttachNote() {
		return false;
	}
	
	/**
	 * @generated NOT
	 */
	@Override
	public boolean isSelectable() {
		return true;
	}

	/**
	 * @generated NOT
	 */
	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE,
				new org.wso2.developerstudio.datamapper.diagram.edit.policies.DataMapperLinkItemSemanticEditPolicy());

		installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, new CustomNonResizableEditPolicyEx()); // remove
		installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, new CustomNonResizableEditPolicyEx()); // selection

		removeEditPolicy(org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles.POPUPBAR_ROLE);
	}

	/**
	 * Creates figure for this edit part.
	 * 
	 * Body of this method does not depend on settings in generation model so
	 * you may safely remove <i>generated</i> tag and modify it.
	 * 
	 * @generated NOT
	 */
	protected Connection createConnectionFigure() {
		PolylineConnection connection = new PolylineConnection() {

			@Override
			public void paintFigure(Graphics graphics) {
				graphics.setAlpha(alpha);
				graphics.setLineWidth(3);
				graphics.setBackgroundColor(DataMapperColorConstants.connectorColor);
				graphics.setForegroundColor(DataMapperColorConstants.connectorColor);
				super.paintFigure(graphics);
				
			}

			public PointList getPoints() {
				
				if (getTarget() instanceof InNode2Figure) {
					((InNode2Figure) getTarget()).highlightElementOnSelection();
				}else if (getTarget() instanceof InNode3Figure) {
					((InNode3Figure) getTarget()).highlightElementOnSelection();
				}else if (getTarget() instanceof InNodeFigure) {
					((InNodeFigure) getTarget()).highlightElementOnSelection();
				}
				if (getSource() instanceof OutNode2Figure){
				}
				PointList list = super.getPoints();
				if (list.size() == 0) {
					return list;
				}
				Point start = getStart();
				int difflength = 30;
				Point start2 = new Point(start.x + difflength, start.y);
				Point end = new Point(getEnd().x, getEnd().y);
				Point end2 = new Point(end.x - difflength, end.y);
				list.removeAllPoints();
				list.addPoint(start);
				list.addPoint(start2);
				list.addPoint(end2);
				list.addPoint(end);
				return list;
			}
		};
		connection.setConnectionRouter(new ManhattanConnectionRouter());
		return connection;
	}

	/**
	 * @generated
	 */
	public PolylineConnectionEx getPrimaryShape() {
		return (PolylineConnectionEx) getFigure();
	}

}
