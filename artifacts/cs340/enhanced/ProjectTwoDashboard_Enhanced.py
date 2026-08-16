"""
Enhanced dashboard entry point for CS 340 Project Two.

This version keeps the original dashboard purpose but removes hard-coded credentials
and routes all data requests through CRUD_Python_Module_Enhanced.AnimalShelter.
"""

from jupyter_dash import JupyterDash
import dash_leaflet as dl
from dash import dash_table, dcc, html
from dash.dependencies import Input, Output
import plotly.express as px
import pandas as pd

from CRUD_Python_Module_Enhanced import AnimalShelter, DatabaseOperationError, QueryValidationError
from database_config import MongoSettings

JupyterDash.infer_jupyter_proxy_config()

DEFAULT_LIMIT = MongoSettings.from_env().default_limit
shelter = AnimalShelter.from_env()


def dataframe_from_records(records):
    """Convert MongoDB records into a dashboard-safe pandas DataFrame."""
    data = pd.DataFrame.from_records(records)
    if data.empty:
        return data

    if "_id" in data.columns:
        data.drop(columns=["_id"], inplace=True)

    for col in ["age_upon_outcome_in_weeks", "location_lat", "location_long"]:
        if col in data.columns:
            data[col] = pd.to_numeric(data[col], errors="coerce")

    if "location_lat" in data.columns and "location_long" in data.columns:
        data = data.dropna(subset=["location_lat", "location_long"]).reset_index(drop=True)
    return data


def read_data(filter_type):
    """Read dashboard records through the enhanced, allowlisted data layer."""
    try:
        if filter_type == "reset":
            records = shelter.read({}, limit=DEFAULT_LIMIT)
        else:
            records = shelter.find_rescue_candidates(filter_type, limit=DEFAULT_LIMIT)
        return dataframe_from_records(records)
    except (DatabaseOperationError, QueryValidationError) as exc:
        print(f"Dashboard data request failed: {exc}")
        return pd.DataFrame()


initial_df = read_data("reset")

app = JupyterDash("ProjectTwoDashboardEnhanced")
app.layout = html.Div(
    style={"backgroundColor": "#EEF3F8", "padding": "20px", "fontFamily": "Arial, sans-serif"},
    children=[
        html.Div(
            style={"backgroundColor": "white", "padding": "24px", "borderRadius": "12px", "marginBottom": "20px"},
            children=[
                html.H1("Asher's AAC Dashboard", style={"textAlign": "center", "color": "#163A63"}),
                html.H4("Unique Identifier: Asher", style={"textAlign": "center", "color": "#4E6E8E"}),
                html.P(
                    "Grazioso Salvare Rescue Dog Selection Dashboard",
                    style={"textAlign": "center", "color": "#5C6B7A"},
                ),
            ],
        ),
        html.Div(
            style={"backgroundColor": "white", "padding": "18px 22px", "borderRadius": "10px", "marginBottom": "20px"},
            children=[
                html.H3("Rescue Type Filters", style={"color": "#163A63"}),
                dcc.RadioItems(
                    id="filter-type",
                    options=[
                        {"label": " Water Rescue", "value": "water"},
                        {"label": " Mountain or Wilderness Rescue", "value": "mountain"},
                        {"label": " Disaster or Individual Tracking", "value": "disaster"},
                        {"label": " Reset", "value": "reset"},
                    ],
                    value="reset",
                    labelStyle={"display": "inline-block", "marginRight": "20px", "fontWeight": "600"},
                ),
            ],
        ),
        dash_table.DataTable(
            id="datatable-id",
            columns=[{"name": i, "id": i} for i in initial_df.columns],
            data=initial_df.to_dict("records"),
            page_size=10,
            sort_action="native",
            filter_action="native",
            row_selectable="single",
            selected_rows=[0] if not initial_df.empty else [],
            style_table={"overflowX": "auto", "backgroundColor": "white"},
            style_header={"backgroundColor": "#1F3A5F", "color": "white", "fontWeight": "bold"},
            style_cell={"textAlign": "left", "padding": "8px", "fontSize": "13px", "whiteSpace": "normal"},
        ),
        html.Div(
            style={"display": "grid", "gridTemplateColumns": "1fr 1fr", "gap": "20px", "marginTop": "20px"},
            children=[
                html.Div([html.H3("Breed Distribution"), dcc.Graph(id="breed-chart")]),
                html.Div([html.H3("Selected Animal Location"), html.Div(id="map-id")]),
            ],
        ),
    ],
)


@app.callback(
    Output("datatable-id", "data"),
    Output("datatable-id", "columns"),
    Output("datatable-id", "selected_rows"),
    Input("filter-type", "value"),
)
def update_table(filter_type):
    dff = read_data(filter_type)
    if dff.empty:
        return [], [{"name": i, "id": i} for i in initial_df.columns], []
    return dff.to_dict("records"), [{"name": i, "id": i} for i in dff.columns], [0]


@app.callback(Output("breed-chart", "figure"), Input("datatable-id", "derived_virtual_data"))
def update_chart(view_data):
    if not view_data:
        return px.pie(title="No data available")
    dff = pd.DataFrame.from_dict(view_data)
    if "breed" not in dff.columns:
        return px.pie(title="Breed data unavailable")
    breed_counts = dff["breed"].value_counts().head(10).reset_index()
    breed_counts.columns = ["breed", "count"]
    fig = px.pie(breed_counts, names="breed", values="count", title="Breed Distribution")
    fig.update_layout(title_x=0.5)
    return fig


@app.callback(
    Output("map-id", "children"),
    Input("datatable-id", "derived_virtual_data"),
    Input("datatable-id", "derived_virtual_selected_rows"),
)
def update_map(view_data, selected_rows):
    if not view_data:
        return html.Div("No data is available for the map.", style={"textAlign": "center", "color": "#555"})

    dff = pd.DataFrame.from_dict(view_data)
    row = selected_rows[0] if selected_rows else 0
    if row >= len(dff):
        row = 0

    required_cols = {"location_lat", "location_long"}
    if not required_cols.issubset(dff.columns):
        return html.Div("Location fields are unavailable for this record.")

    latitude = dff.iloc[row]["location_lat"]
    longitude = dff.iloc[row]["location_long"]
    animal_name = dff.iloc[row]["name"] if "name" in dff.columns else "Unknown"
    breed = dff.iloc[row]["breed"] if "breed" in dff.columns else "Unknown"
    animal_type = dff.iloc[row]["animal_type"] if "animal_type" in dff.columns else "Unknown"

    return [
        dl.Map(
            style={"width": "100%", "height": "500px", "borderRadius": "10px"},
            center=[latitude, longitude],
            zoom=10,
            children=[
                dl.TileLayer(id="base-layer-id"),
                dl.Marker(
                    position=[latitude, longitude],
                    children=[
                        dl.Tooltip(str(breed)),
                        dl.Popup([
                            html.H2("Animal Name", style={"marginBottom": "10px"}),
                            html.P(str(animal_name)),
                            html.P(f"Breed: {breed}"),
                            html.P(f"Animal Type: {animal_type}"),
                        ]),
                    ],
                ),
            ],
        )
    ]


if __name__ == "__main__":
    app.run_server()
